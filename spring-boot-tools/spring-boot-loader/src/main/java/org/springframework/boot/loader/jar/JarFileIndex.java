/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.loader.jar;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

import org.springframework.boot.loader.data.RandomAccessData;
import org.springframework.boot.loader.data.RandomAccessData.ResourceAccess;

/**
 * Maintains an index of entries.
 *
 * @author Phillip Webb
 */
class JarFileIndex implements CentralDirectoryVistor {

	private static final long LOCAL_FILE_HEADER_SIZE = 30;

	private static final String SLASH = "/";

	private static final String NO_SUFFIX = "";

	private final JarFile jarFile;

	private final JarEntryFilter filter;

	private RandomAccessData centralDirectoryData;

	private int size;

	private int[] hashCodes;

	private int[] centralDirectoryOffsets;

	private int[] positions;

	private SoftReference<JarFileEntry[]> entries = new SoftReference<JarFileEntry[]>(
			null);

	JarFileIndex(JarFile jarFile, JarEntryFilter filter) {
		this.jarFile = jarFile;
		this.filter = filter;
	}

	@Override
	public void visitStart(CentralDirectoryEndRecord endRecord,
			RandomAccessData centralDirectoryData) {
		int maxSize = endRecord.getNumberOfRecords();
		this.centralDirectoryData = centralDirectoryData;
		this.hashCodes = new int[maxSize];
		this.centralDirectoryOffsets = new int[maxSize];
		this.positions = new int[maxSize];
	}

	@Override
	public void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset) {
		AsciiBytes name = applyFilter(fileHeader.getName());
		if (name != null) {
			add(name, fileHeader, dataOffset);
		}
	}

	private void add(AsciiBytes name, CentralDirectoryFileHeader fileHeader,
			int dataOffset) {
		this.hashCodes[this.size] = name.hashCode();
		this.centralDirectoryOffsets[this.size] = dataOffset;
		this.positions[this.size] = this.size;
		this.size++;
	}

	@Override
	public void visitEnd() {
		sort(0, this.size - 1);
		int[] positions = this.positions;
		this.positions = new int[positions.length];
		for (int i = 0; i < this.size; i++) {
			this.positions[positions[i]] = i;
		}
	}

	private void sort(int left, int right) {
		if (left < right) {
			int pivot = this.hashCodes[left + (right - left) / 2];
			int i = left;
			int j = right;
			while (i <= j) {
				while (this.hashCodes[i] < pivot) {
					i++;
				}
				while (this.hashCodes[j] > pivot) {
					j--;
				}
				if (i <= j) {
					swap(i, j);
					i++;
					j--;
				}
			}
			if (left < j) {
				sort(left, j);
			}
			if (right > i) {
				sort(i, right);
			}
		}
	}

	private void swap(int i, int j) {
		swap(this.hashCodes, i, j);
		swap(this.centralDirectoryOffsets, i, j);
		swap(this.positions, i, j);
	}

	private void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public Iterator<JarEntry> getEntries() {
		return new EntryIterator(getEntries(true));
	}

	public boolean containsEntry(String name) throws IOException {
		return getEntry(name, getEntries(false), FileHeaderEntry.class) != null;
	}

	public JarFileEntry getEntry(String name) {
		return getEntry(name, getEntries(true), JarFileEntry.class);
	}

	public InputStream getInputStream(String name, ResourceAccess access)
			throws IOException {
		FileHeaderEntry entry = getEntry(name, getEntries(false), FileHeaderEntry.class);
		return getInputStream(entry, access);
	}

	public InputStream getInputStream(FileHeaderEntry entry, ResourceAccess access)
			throws IOException {
		if (entry == null) {
			return null;
		}
		InputStream inputStream = getEntryData(entry).getInputStream(access);
		if (entry.getMethod() == ZipEntry.DEFLATED) {
			inputStream = new ZipInflaterInputStream(inputStream, (int) entry.getSize());
		}
		return inputStream;
	}

	public RandomAccessData getEntryData(String name) throws IOException {
		FileHeaderEntry entry = getEntry(name, this.entries.get(), FileHeaderEntry.class);
		if (entry == null) {
			return null;
		}
		return getEntryData(entry);
	}

	private RandomAccessData getEntryData(FileHeaderEntry entry) throws IOException {
		// aspectjrt-1.7.4.jar has a different ext bytes length in the
		// local directory to the central directory. We need to re-read
		// here to skip them
		RandomAccessData data = this.jarFile.getData();
		byte[] localHeader = Bytes.get(
				data.getSubsection(entry.getLocalHeaderOffset(), LOCAL_FILE_HEADER_SIZE));
		long nameLength = Bytes.littleEndianValue(localHeader, 26, 2);
		long extraLength = Bytes.littleEndianValue(localHeader, 28, 2);
		return data.getSubsection(entry.getLocalHeaderOffset() + LOCAL_FILE_HEADER_SIZE
				+ nameLength + extraLength, entry.getCompressedSize());
	}

	private JarFileEntry[] getEntries(boolean create) {
		JarFileEntry[] entries = this.entries.get();
		if (entries == null && create) {
			entries = new JarFileEntry[this.size];
			this.entries = new SoftReference<JarFileEntry[]>(entries);
		}
		return entries;
	}

	private <T extends FileHeaderEntry> T getEntry(String name, JarFileEntry[] entries,
			Class<T> type) {
		int hashCode = AsciiBytes.hashCode(name);
		T entry = getEntry(entries, hashCode, name, NO_SUFFIX, type);
		if (entry == null) {
			hashCode = AsciiBytes.hashCode(hashCode, SLASH);
			entry = getEntry(entries, hashCode, name, SLASH, type);
		}
		return entry;
	}

	private <T extends FileHeaderEntry> T getEntry(JarFileEntry[] entries, int hashCode,
			String name, String suffix, Class<T> type) {
		int index = getFirstIndex(hashCode);
		while (index >= 0 && index < this.size && this.hashCodes[index] == hashCode) {
			T entry = getEntry(entries, index, type);
			if (entry.hasName(name, suffix)) {
				return entry;
			}
			index++;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T extends FileHeaderEntry> T getEntry(JarFileEntry[] entries, int index,
			Class<T> type) {
		JarFileEntry entry = (entries == null ? null : entries[index]);
		if (entry != null) {
			return (T) entry;
		}
		try {
			CentralDirectoryFileHeader header = CentralDirectoryFileHeader
					.fromRandomAccessData(this.centralDirectoryData,
							this.centralDirectoryOffsets[index]);
			if (FileHeaderEntry.class.equals(type)) {
				// No need to convert
				return (T) header;
			}
			entry = new JarFileEntry(this.jarFile,
					applyFilter(header.getName()).toString(), header);
			if (entries != null) {
				entries[index] = entry;
			}
			return (T) entry;
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private int getFirstIndex(int hashCode) {
		int index = Arrays.binarySearch(this.hashCodes, 0, this.size, hashCode);
		if (index < 0) {
			return -1;
		}
		while (index > 0 && this.hashCodes[index - 1] == hashCode) {
			index--;
		}
		return index;
	}

	private AsciiBytes applyFilter(AsciiBytes name) {
		return (this.filter == null ? name : this.filter.apply(name));
	}

	private class EntryIterator implements Iterator<JarEntry> {

		private int index = 0;

		private JarFileEntry[] entries;

		EntryIterator(JarFileEntry[] entries) {
			this.entries = entries;
		}

		@Override
		public boolean hasNext() {
			return this.index < this.entries.length;
		}

		@Override
		public JarEntry next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			int entryIndex = JarFileIndex.this.positions[this.index];
			this.index++;
			return getEntry(this.entries, entryIndex, JarFileEntry.class);
		}

	}

}

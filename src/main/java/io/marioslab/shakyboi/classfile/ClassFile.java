package io.marioslab.shakyboi.classfile;

import java.util.ArrayList;
import java.util.List;

public class ClassFile {
    public static final int CONSTANT_DOUBLE = 6;
    public static final int CONSTANT_CLASS = 7;
    public static final int CONSTANT_STRING = 8;
    public static final int CONSTANT_FIELDREF = 9;
    public static final int CONSTANT_METHODREF = 10;
    public static final int CONSTANT_INTERFACEMETHODREF = 11;
    public static final int CONSTANT_NAMEANDTYPE = 12;
    public static final int CONSTANT_METHODHANDLE = 15;
    public static final int CONSTANT_METHODTYPE = 16;
    public static final int CONSTANT_DYNAMIC = 17;
    public static final int CONSTANT_INVOKEDYNAMIC = 18;
    public static final int CONSTANT_MODULE = 19;
    public static final int CONSTANT_PACKAGE = 20;
    public static final int CONSTANT_UTF8 = 1;
    public static final int CONSTANT_INTEGER = 3;
    public static final int CONSTANT_FLOAT = 4;
    public static final int CONSTANT_LONG = 5;

    public String sourceFile;
    public byte[] originalData;
    public int magic = 0xcafebabe;
    public int minorVersion;
    public int majorVersion;
    public final List<ConstantPoolEntry> constantPool = new ArrayList<>();
    public int accessFlags;
    public int thisClass;
    public int superClass;
    public final List<Integer> interfaces = new ArrayList<>();
    public final List<MemberInfo> fields = new ArrayList<>();
    public final List<MemberInfo> methods = new ArrayList<>();
    public final List<AttributeInfo> attributes = new ArrayList<>();

    public ClassFile(String sourceFile, byte[] originalData) {
        this.sourceFile = sourceFile;
        this.originalData = originalData;
    }

    public String getUtf8String(int index) {
        return new String(((Utf8PoolEntry) constantPool.get(index)).bytes);
    }

    public String getName() {
        ClassPoolEntry thisClassEntry = (ClassPoolEntry) constantPool.get(thisClass);
        return getUtf8String(thisClassEntry.nameIndex);
    }

    public String getSuperClassName() {
        ClassPoolEntry superClassEntry = (ClassPoolEntry) constantPool.get(superClass);
        return getUtf8String(superClassEntry.nameIndex);
    }

    public String toString() {
        return sourceFile;
    }

    public abstract static class ConstantPoolEntry {
        public ClassFile clazz;
        public int tag;

        public ConstantPoolEntry(ClassFile clz, int tag) {
            this.clazz = clz;
            this.tag = tag;
        }
    }

    public static class Utf8PoolEntry extends ConstantPoolEntry {
        public byte[] bytes;

        public Utf8PoolEntry(ClassFile clz, int tag, byte[] bytes) {
            super(clz, tag);
            this.bytes = bytes;
        }
    }

    public static class IntPoolEntry extends ConstantPoolEntry {
        public int value;

        public IntPoolEntry(ClassFile clz, int tag, int value) {
            super(clz, tag);
            this.value = value;
        }
    }

    public static class FloatPoolEntry extends ConstantPoolEntry {
        public float value;

        public FloatPoolEntry(ClassFile clz, int tag, float value) {
            super(clz, tag);
            this.value = value;
        }
    }

    public static class LongPoolEntry extends ConstantPoolEntry {
        public int highBytes;
        public int lowBytes;

        public LongPoolEntry(ClassFile clz, int tag, int highBytes, int lowBytes) {
            super(clz, tag);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class DoublePoolEntry extends ConstantPoolEntry {
        public int highBytes;
        public int lowBytes;

        public DoublePoolEntry(ClassFile clz, int tag, int highBytes, int lowBytes) {
            super(clz, tag);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class PackagePoolEntry extends ConstantPoolEntry {
        public int nameIndex;

        public PackagePoolEntry(ClassFile clz, int tag, int nameIndex) {
            super(clz, tag);
            this.nameIndex = nameIndex;
        }
    }

    public static class ModulePoolEntry extends ConstantPoolEntry {
        public int nameIndex;

        public ModulePoolEntry(ClassFile clz, int tag, int nameIndex) {
            super(clz, tag);
            this.nameIndex = nameIndex;
        }
    }

    public static class ClassPoolEntry extends ConstantPoolEntry {
        public int nameIndex;

        public ClassPoolEntry(ClassFile clz, int tag, int nameIndex) {
            super(clz, tag);
            this.nameIndex = nameIndex;
        }
    }

    public static class StringPoolEntry extends ConstantPoolEntry {
        public int stringIndex;

        public StringPoolEntry(ClassFile clz, int tag, int stringIndex) {
            super(clz, tag);
            this.stringIndex = stringIndex;
        }
    }

    public static class FieldRefPoolEntry extends ConstantPoolEntry {
        public int classIndex;
        public int nameAndTypeIndex;

        public FieldRefPoolEntry(ClassFile clz, int tag, int classIndex, int nameAndTypeIndex) {
            super(clz, tag);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class MethodRefPoolEntry extends ConstantPoolEntry {
        public int classIndex;
        public int nameAndTypeIndex;

        public MethodRefPoolEntry(ClassFile clz, int tag, int classIndex, int nameAndTypeIndex) {
            super(clz, tag);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class InterfaceMethodRefPoolEntry extends ConstantPoolEntry {
        public int classIndex;
        public int nameAndTypeIndex;

        public InterfaceMethodRefPoolEntry(ClassFile clz, int tag, int classIndex, int nameAndTypeIndex) {
            super(clz, tag);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class NameAndTypePoolEntry extends ConstantPoolEntry {
        public int nameIndex;
        public int descriptorIndex;

        public NameAndTypePoolEntry(ClassFile clz, int tag, int nameIndex, int descriptorIndex) {
            super(clz, tag);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class MethodHandlePoolEntry extends ConstantPoolEntry {
        public int referenceKind;
        public int referenceIndex;

        public MethodHandlePoolEntry(ClassFile clz, int tag, int referenceKind, int referenceIndex) {
            super(clz, tag);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
        }
    }

    public static class MethodTypePoolEntry extends ConstantPoolEntry {
        public int descriptorIndex;

        public MethodTypePoolEntry(ClassFile clz, int tag, int descriptorIndex) {
            super(clz, tag);
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class DynamicPoolEntry extends ConstantPoolEntry {
        public int bootstrapMethodAttributeIndex;
        public int nameAndTypeIndex;

        public DynamicPoolEntry(ClassFile clz, int tag, int bootstrapMethodAttributeIndex, int nameAndTypeIndex) {
            super(clz, tag);
            this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class InvokeDynamicPoolEntry extends ConstantPoolEntry {
        public int bootstrapMethodAttributeIndex;
        public int nameAndTypeIndex;

        public InvokeDynamicPoolEntry(ClassFile clz, int tag, int bootstrapMethodAttributeIndex, int nameAndTypeIndex) {
            super(clz, tag);
            this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class MemberInfo {
        public int accessFlags;
        public int nameIndex;
        public int descriptorIndex;
        public final ArrayList<AttributeInfo> attributes = new ArrayList<>();
        public ClassFile clazz;

        public MemberInfo(ClassFile clz, int accessFlags, int nameIndex, int descriptorIndex) {
            this.clazz = clz;
            this.accessFlags = accessFlags;
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        public String getName() {
            return clazz.getUtf8String(nameIndex);
        }

        public String getDescriptor() {
            return clazz.getUtf8String(descriptorIndex);
        }

        public String toString() {
            return getName() + getDescriptor();
        }
    }

    public static class AttributeInfo {
        public final ClassFile clz;
        public int nameIndex;
        public byte[] info;

        public AttributeInfo(ClassFile clz, int nameIndex, byte[] info) {
            this.clz = clz;
            this.nameIndex = nameIndex;
            this.info = info;
        }

        public String getName() {
            return clz.getUtf8String(nameIndex);
        }
    }
}

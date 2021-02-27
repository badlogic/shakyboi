package io.marioslab.shakyboi.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A parsed representation of a <code>.class</code> file.
 *
 * @see ClassFileReader
 * @see ClassFileWriter
 */
public class ClassFile {
    // Constant pool tags
    public static final int CONSTANT_UTF8 = 1;
    public static final int CONSTANT_INTEGER = 3;
    public static final int CONSTANT_FLOAT = 4;
    public static final int CONSTANT_LONG = 5;
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

    // Access flags
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_SUPER = 0x0020;
    public static final int ACC_VOLATILE = 0x0040;
    public static final int ACC_BRIDGE = 0x0040;
    public static final int ACC_TRANSIENT = 0x0080;
    public static final int ACC_VARARGS = 0x0080;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_INTERFACE = 0x0200;
    public static final int ACC_ABSTRACT = 0x0400;
    public static final int ACC_STRICT = 0x0800;
    public static final int ACC_SYNTHETIC = 0x1000;
    public static final int ACC_ANNOTATION = 0x2000;
    public static final int ACC_ENUM = 0x4000;
    public static final int ACC_MODULE = 0x8000;

    // Reference kinds
    public static final int REF_GETFIELD = 1;
    public static final int REF_GETSTATIC = 2;
    public static final int REF_PUTFIELD = 3;
    public static final int REF_PUTSTATIC = 4;
    public static final int REF_INVOKEVIRTUAL = 5;
    public static final int REF_INVOKESTATIC = 6;
    public static final int REF_INVOKESPECIAL = 7;
    public static final int REF_NEWINVOKESPECIAL = 8;
    public static final int REF_INVOKEINTERFACE = 9;

    // Attributes
    public static final String ATTR_CONSTANTVALUE = "ConstantValue";
    public static final String ATTR_CODE = "Code";


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
        return new String(((Utf8Entry) constantPool.get(index)).bytes, StandardCharsets.UTF_8);
    }

    public <T> T getConstantPoolEntry(int index) {
        return (T) constantPool.get(index);
    }

    public String getName() {
        ClassInfoEntry thisClassEntry = (ClassInfoEntry) constantPool.get(thisClass);
        return getUtf8String(thisClassEntry.nameIndex);
    }

    public String getSuperClassName() {
        ClassInfoEntry superClassEntry = (ClassInfoEntry) constantPool.get(superClass);
        return getUtf8String(superClassEntry.nameIndex);
    }

    public boolean hasAccessFlag(int flag) {
        return (accessFlags & flag) != 0;
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

    public static class Utf8Entry extends ConstantPoolEntry {
        public byte[] bytes;

        public Utf8Entry(ClassFile clz, byte[] bytes) {
            super(clz, CONSTANT_UTF8);
            this.bytes = bytes;
        }
    }

    public static class IntEntry extends ConstantPoolEntry {
        public int value;

        public IntEntry(ClassFile clz, int value) {
            super(clz, CONSTANT_INTEGER);
            this.value = value;
        }
    }

    public static class FloatEntry extends ConstantPoolEntry {
        public float value;

        public FloatEntry(ClassFile clz, float value) {
            super(clz, CONSTANT_FLOAT);
            this.value = value;
        }
    }

    public static class LongEntry extends ConstantPoolEntry {
        public int highBytes;
        public int lowBytes;

        public LongEntry(ClassFile clz, int highBytes, int lowBytes) {
            super(clz, CONSTANT_LONG);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class DoubleEntry extends ConstantPoolEntry {
        public int highBytes;
        public int lowBytes;

        public DoubleEntry(ClassFile clz, int highBytes, int lowBytes) {
            super(clz, CONSTANT_DOUBLE);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class PackageEntry extends ConstantPoolEntry {
        public int nameIndex;

        public PackageEntry(ClassFile clz, int nameIndex) {
            super(clz, CONSTANT_PACKAGE);
            this.nameIndex = nameIndex;
        }

        public String getName() {
            return clazz.getUtf8String(nameIndex);
        }
    }

    public static class ModuleEntry extends ConstantPoolEntry {
        public int nameIndex;

        public ModuleEntry(ClassFile clz, int nameIndex) {
            super(clz, CONSTANT_MODULE);
            this.nameIndex = nameIndex;
        }

        public String getName() {
            return clazz.getUtf8String(nameIndex);
        }
    }

    public static class ClassInfoEntry extends ConstantPoolEntry {
        public int nameIndex;

        public ClassInfoEntry(ClassFile clz, int nameIndex) {
            super(clz, CONSTANT_CLASS);
            this.nameIndex = nameIndex;
        }

        public String getName() {
            return clazz.getUtf8String(nameIndex);
        }
    }

    public static class StringEntry extends ConstantPoolEntry {
        public int stringIndex;

        public StringEntry(ClassFile clz, int stringIndex) {
            super(clz, CONSTANT_STRING);
            this.stringIndex = stringIndex;
        }

        public String getString() {
            return clazz.getUtf8String(stringIndex);
        }
    }

    public static class FieldRefEntry extends ConstantPoolEntry {
        public int classIndex;
        public int nameAndTypeIndex;

        public FieldRefEntry(ClassFile clz, int classIndex, int nameAndTypeIndex) {
            super(clz, CONSTANT_FIELDREF);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public NameAndTypeEntry getNameAndType() {
            return clazz.getConstantPoolEntry(nameAndTypeIndex);
        }
    }

    public static class MethodRefEntry extends ConstantPoolEntry {
        public int classIndex;
        public int nameAndTypeIndex;

        public MethodRefEntry(ClassFile clz, int classIndex, int nameAndTypeIndex) {
            super(clz, CONSTANT_METHODREF);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public ClassInfoEntry getClassInfo() {
            return clazz.getConstantPoolEntry(classIndex);
        }

        public NameAndTypeEntry getNameAndType() {
            return clazz.getConstantPoolEntry(nameAndTypeIndex);
        }
    }

    public static class InterfaceMethodRefEntry extends ConstantPoolEntry {
        public int classIndex;
        public int nameAndTypeIndex;

        public InterfaceMethodRefEntry(ClassFile clz, int classIndex, int nameAndTypeIndex) {
            super(clz, CONSTANT_INTERFACEMETHODREF);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public ClassInfoEntry getClassInfo() {
            return clazz.getConstantPoolEntry(classIndex);
        }

        public NameAndTypeEntry getNameAndType() {
            return clazz.getConstantPoolEntry(nameAndTypeIndex);
        }
    }

    public static class NameAndTypeEntry extends ConstantPoolEntry {
        public int nameIndex;
        public int descriptorIndex;

        public NameAndTypeEntry(ClassFile clz, int nameIndex, int descriptorIndex) {
            super(clz, CONSTANT_NAMEANDTYPE);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        public String getName() {
            return clazz.getUtf8String(nameIndex);
        }

        public String getDescriptor() {
            return clazz.getUtf8String(descriptorIndex);
        }
    }

    public static class MethodHandleEntry extends ConstantPoolEntry {
        public int referenceKind;
        public int referenceIndex;

        public MethodHandleEntry(ClassFile clz, int referenceKind, int referenceIndex) {
            super(clz, CONSTANT_METHODHANDLE);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
        }
    }

    public static class MethodTypeEntry extends ConstantPoolEntry {
        public int descriptorIndex;

        public MethodTypeEntry(ClassFile clz, int descriptorIndex) {
            super(clz, CONSTANT_METHODTYPE);
            this.descriptorIndex = descriptorIndex;
        }

        public String getDescriptor() {
            return clazz.getUtf8String(descriptorIndex);
        }
    }

    public static class DynamicEntry extends ConstantPoolEntry {
        public int bootstrapMethodAttributeIndex;
        public int nameAndTypeIndex;

        public DynamicEntry(ClassFile clz, int bootstrapMethodAttributeIndex, int nameAndTypeIndex) {
            super(clz, CONSTANT_DYNAMIC);
            this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public NameAndTypeEntry getNameAndType() {
            return clazz.getConstantPoolEntry(nameAndTypeIndex);
        }
    }

    public static class InvokeDynamicEntry extends ConstantPoolEntry {
        public int bootstrapMethodAttributeIndex;
        public int nameAndTypeIndex;

        public InvokeDynamicEntry(ClassFile clz, int bootstrapMethodAttributeIndex, int nameAndTypeIndex) {
            super(clz, CONSTANT_INVOKEDYNAMIC);
            this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public NameAndTypeEntry getNameAndType() {
            return clazz.getConstantPoolEntry(nameAndTypeIndex);
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

        public boolean hasAccessFlag(int flag) {
            return (accessFlags & flag) != 0;
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
        protected byte[] info;

        public AttributeInfo(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            this.clz = clz;
            this.nameIndex = nameIndex;
            this.info = info.readNBytes(length);
        }

        public String getName() {
            return clz.getUtf8String(nameIndex);
        }

        public void write(DataOutputStream out) throws IOException {
            out.writeInt(info.length);
            out.write(info);
        }
    }

    public static class ConstantValueAttribute extends AttributeInfo {
        public ConstantValueAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class CodeAttribute extends AttributeInfo {
        public CodeAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class StackMapTableAttribute extends AttributeInfo {
        public StackMapTableAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class ExceptionsAttribute extends AttributeInfo {
        public ExceptionsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class InnerClassesAttribute extends AttributeInfo {
        public InnerClassesAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class EnclosingMethodAttribute extends AttributeInfo {
        public EnclosingMethodAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class SyntheticAttribute extends AttributeInfo {
        public SyntheticAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class SignatureAttribute extends AttributeInfo {
        public SignatureAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class SourceFileAttribute extends AttributeInfo {
        public SourceFileAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class SourceDebugExtensionAttribute extends AttributeInfo {
        public SourceDebugExtensionAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class LineNumberTableAttribute extends AttributeInfo {
        public LineNumberTableAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class LocalVariableTableAttribute extends AttributeInfo {
        public LocalVariableTableAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class LocalVariableTypeTableAttribute extends AttributeInfo {
        public LocalVariableTypeTableAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class DeprecatedAttribute extends AttributeInfo {
        public DeprecatedAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
        }
    }

    public static class RuntimeVisibleAnnotationsAttribute extends AttributeInfo {
        public RuntimeVisibleAnnotationsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class RuntimeInvisibleAnnotationsAttribute extends AttributeInfo {
        public RuntimeInvisibleAnnotationsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class RuntimeVisibleParameterAnnotationsAttribute extends AttributeInfo {
        public RuntimeVisibleParameterAnnotationsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class RuntimeInvisibleParameterAnnotationsAttribute extends AttributeInfo {
        public RuntimeInvisibleParameterAnnotationsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class RuntimeVisibleTypeAnnotationsAttribute extends AttributeInfo {
        public RuntimeVisibleTypeAnnotationsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class RuntimeInvisibleTypeAnnotationsAttribute extends AttributeInfo {
        public RuntimeInvisibleTypeAnnotationsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class AnnotationDefaultAttribute extends AttributeInfo {
        public AnnotationDefaultAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class BootstrapMethodsAttribute extends AttributeInfo {
        public BootstrapMethodsAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class MethodParametersAttribute extends AttributeInfo {
        public MethodParametersAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class ModuleAttribute extends AttributeInfo {
        public ModuleAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class ModulePackagesAttribute extends AttributeInfo {
        public ModulePackagesAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class ModuleMainClassAttribute extends AttributeInfo {
        public ModuleMainClassAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class NestHostAttribute extends AttributeInfo {
        public NestHostAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }

    public static class NestMembersAttribute extends AttributeInfo {
        public NestMembersAttribute(ClassFile clz, int nameIndex, int length, DataInputStream info) throws IOException {
            super(clz, nameIndex, length, info);
            // TODO
        }
    }
}

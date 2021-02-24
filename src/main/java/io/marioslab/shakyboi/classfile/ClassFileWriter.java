package io.marioslab.shakyboi.classfile;

import io.marioslab.shakyboi.util.ByteArrayOutput;

import java.io.IOException;
import java.util.List;

/**
 * Writes a {@link ClassFile} to a byte array according to the <code>.class</code> file format specification.
 * 
 * @see {@link #writeClassFile(ClassFile)}
 * 
 * See <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html">The Java Virtual Machine Specification</a>.
 */
public class ClassFileWriter {
    public static byte[] writeClassFile(ClassFile clz) throws IOException {
        ByteArrayOutput out = new ByteArrayOutput(8192);

        out.writeInt(clz.magic);
        out.writeShort(clz.minorVersion);
        out.writeShort(clz.majorVersion);
        out.writeShort(clz.constantPool.size());

        // Skip first entry, skip null entries for long/double
        for (int i = 1; i < clz.constantPool.size(); i++)
            writeConstantPoolEntry(out, clz.constantPool.get(i));

        out.writeShort(clz.accessFlags);
        out.writeShort(clz.thisClass);
        out.writeShort(clz.superClass);

        out.writeShort(clz.interfaces.size());
        for (int i = 0; i < clz.interfaces.size(); i++)
            out.writeShort(clz.interfaces.get(i));

        out.writeShort(clz.fields.size());
        for (int i = 0; i < clz.fields.size(); i++)
            writeMember(out, clz.fields.get(i));

        out.writeShort(clz.methods.size());
        for (int i = 0; i < clz.methods.size(); i++) {
            writeMember(out, clz.methods.get(i));
        }

        out.writeShort(clz.attributes.size());
        for (int i = 0; i < clz.attributes.size(); i++)
            writeAttribute(out, clz.attributes.get(i));

        return out.toByteArray();
    }

    public static void writeMember(ByteArrayOutput out, ClassFile.MemberInfo member) throws IOException {
        out.writeShort(member.accessFlags);
        out.writeShort(member.nameIndex);
        out.writeShort(member.descriptorIndex);

        writeAttributes(out, member.attributes);
    }

    public static void writeAttributes(ByteArrayOutput out, List<ClassFile.AttributeInfo> attributes) throws IOException {
        out.writeShort(attributes.size());
        for (ClassFile.AttributeInfo attribute : attributes)
            writeAttribute(out, attribute);

    }

    public static void writeAttribute(ByteArrayOutput out, ClassFile.AttributeInfo attribute) throws IOException {
        out.writeShort(attribute.nameIndex);
        out.writeInt(attribute.info.length);
        out.write(attribute.info);
    }

    public static void writeConstantPoolEntry(ByteArrayOutput out, ClassFile.ConstantPoolEntry entry) throws IOException {
        if (entry == null) return;

        int tag = entry.tag;
        out.write(tag);

        if (entry instanceof ClassFile.Utf8Entry) {
            ClassFile.Utf8Entry utf8 = (ClassFile.Utf8Entry) entry;
            out.writeShort(utf8.bytes.length);
            out.write(utf8.bytes);
        } else if (entry instanceof ClassFile.IntEntry) {
            out.writeInt(((ClassFile.IntEntry) entry).value);
        } else if (entry instanceof ClassFile.FloatEntry) {
            out.writeFloat(((ClassFile.FloatEntry) entry).value);
        } else if (entry instanceof ClassFile.LongEntry) {
            ClassFile.LongEntry longEntry = (ClassFile.LongEntry) entry;
            out.writeInt(longEntry.highBytes);
            out.writeInt(longEntry.lowBytes);
        } else if (entry instanceof ClassFile.DoubleEntry) {
            ClassFile.DoubleEntry doubleEntry = (ClassFile.DoubleEntry) entry;
            out.writeInt(doubleEntry.highBytes);
            out.writeInt(doubleEntry.lowBytes);
        } else if (entry instanceof ClassFile.PackageEntry) {
            out.writeShort(((ClassFile.PackageEntry) entry).nameIndex);
        } else if (entry instanceof ClassFile.ModuleEntry) {
            out.writeShort(((ClassFile.ModuleEntry) entry).nameIndex);
        } else if (entry instanceof ClassFile.ClassInfoEntry) {
            out.writeShort(((ClassFile.ClassInfoEntry) entry).nameIndex);
        } else if (entry instanceof ClassFile.StringEntry) {
            out.writeShort(((ClassFile.StringEntry) entry).stringIndex);
        } else if (entry instanceof ClassFile.FieldRefEntry) {
            ClassFile.FieldRefEntry fmiEntry = (ClassFile.FieldRefEntry) entry;
            out.writeShort(fmiEntry.classIndex);
            out.writeShort(fmiEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.MethodRefEntry) {
            ClassFile.MethodRefEntry fmiEntry = (ClassFile.MethodRefEntry) entry;
            out.writeShort(fmiEntry.classIndex);
            out.writeShort(fmiEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.InterfaceMethodRefEntry) {
            ClassFile.InterfaceMethodRefEntry fmiEntry = (ClassFile.InterfaceMethodRefEntry) entry;
            out.writeShort(fmiEntry.classIndex);
            out.writeShort(fmiEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.NameAndTypeEntry) {
            ClassFile.NameAndTypeEntry ntEntry = (ClassFile.NameAndTypeEntry) entry;
            out.writeShort(ntEntry.nameIndex);
            out.writeShort(ntEntry.descriptorIndex);
        } else if (entry instanceof ClassFile.MethodHandleEntry) {
            ClassFile.MethodHandleEntry mEntry = (ClassFile.MethodHandleEntry) entry;
            out.write(mEntry.referenceKind);
            out.writeShort(mEntry.referenceIndex);
        } else if (entry instanceof ClassFile.MethodTypeEntry) {
            out.writeShort(((ClassFile.MethodTypeEntry) entry).descriptorIndex);
        } else if (entry instanceof ClassFile.DynamicEntry) {
            ClassFile.DynamicEntry dEntry = (ClassFile.DynamicEntry) entry;
            out.writeShort(dEntry.bootstrapMethodAttributeIndex);
            out.writeShort(dEntry.nameAndTypeIndex);
        } else if (entry instanceof ClassFile.InvokeDynamicEntry) {
            ClassFile.InvokeDynamicEntry dEntry = (ClassFile.InvokeDynamicEntry) entry;
            out.writeShort(dEntry.bootstrapMethodAttributeIndex);
            out.writeShort(dEntry.nameAndTypeIndex);
        } else {
            throw new RuntimeException("Unknown constant pool entry: " + entry.getClass().getName());
        }
    }
}

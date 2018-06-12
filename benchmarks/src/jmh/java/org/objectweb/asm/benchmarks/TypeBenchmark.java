package org.objectweb.asm.benchmarks;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Thread)
public class TypeBenchmark extends AbstractBenchmark {

  private ArrayList<String> descriptors;
  private ArrayList<String> methodDescriptors;

  public TypeBenchmark() {
    super("org.objectweb.asm.benchmarks.Type");
  }

  @Setup
  public void prepare() throws Exception {
    prepareClasses();
    descriptors = new ArrayList<String>();
    methodDescriptors = new ArrayList<String>();
    for (byte[] classFile : classFiles) {
      new ClassReader(classFile).accept(new CollectTypesVisitor(), 0);
    }
  }

  @Benchmark
  public void getTypeFromDescriptor(final Blackhole blackhole) {
    for (String descriptor : descriptors) {
      blackhole.consume(Type.getType(descriptor));
    }
    for (String methodDescriptor : methodDescriptors) {
      blackhole.consume(Type.getType(methodDescriptor));
    }
  }

  @Benchmark
  public void getArgumentsAndReturnTypesFromDescriptor(final Blackhole blackhole) {
    for (String methodDescriptor : methodDescriptors) {
      Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
      Type returnType = Type.getReturnType(methodDescriptor);
      blackhole.consume(Type.getMethodType(returnType, argumentTypes));
    }
  }

  @Benchmark
  public void getArgumentsAndReturnSizeFromDescriptor(final Blackhole blackhole) {
    for (String methodDescriptor : methodDescriptors) {
      blackhole.consume(Type.getArgumentsAndReturnSizes(methodDescriptor));
    }
  }

  class CollectTypesVisitor extends ClassVisitor {

    AnnotationVisitor annotationVisitor =
        new AnnotationVisitor(api) {

          @Override
          public void visitEnum(final String name, final String descriptor, final String value) {
            descriptors.add(descriptor);
          }

          @Override
          public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
            descriptors.add(descriptor);
            return this;
          }
        };

    CollectTypesVisitor() {
      super(Opcodes.ASM6);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String descriptor) {
      if (descriptor != null) {
        methodDescriptors.add(descriptor);
      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      descriptors.add(descriptor);
      return annotationVisitor;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      descriptors.add(descriptor);
      return annotationVisitor;
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
      descriptors.add(descriptor);
      return new FieldVisitor(api) {

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }
      };
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      methodDescriptors.add(descriptor);
      return new MethodVisitor(api) {

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }

        @Override
        public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
          descriptors.add(descriptor);
        }

        @Override
        public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
          methodDescriptors.add(descriptor);
        }

        @Override
        public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {
          methodDescriptors.add(descriptor);
        }

        @Override
        public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
          descriptors.add(descriptor);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }

        @Override
        public void visitLocalVariable(
            final String name,
            final String descriptor,
            final String signature,
            final Label start,
            final Label end,
            final int index) {
          descriptors.add(descriptor);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(
            final int typeRef,
            final TypePath typePath,
            final Label[] start,
            final Label[] end,
            final int[] index,
            final String descriptor,
            final boolean visible) {
          descriptors.add(descriptor);
          return annotationVisitor;
        }
      };
    }
  }
}

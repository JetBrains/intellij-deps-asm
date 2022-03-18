<!---
Before creating a new issue, consider the following (this does not apply to
feature requests):

- if you need help on how to use ASM, please consult the documentation first
(https://asm.ow2.io/documentation.html). If you can't find the answer there,
describe your problem on the http://mail.ow2.org/wws/info/asm mailing listing
instead of using GitLab issues. The mailing list has more subscribers than the
GitLab issue tracker, and you will thus likely get a quicker answer via the
mailing list.

- check if your *input* classes are valid. For this you can use "javap -p -c -v
<yourclass.class>". If this command fails, the bug is most likely in the tool
that produced this class, not in ASM. Check the javap output carefully:
sometimes the tool is able to print most of the class content, but fails to
parse some part of it. In this case there can be an error message in the middle
of the javap output, which can be easy to miss at first sight.

- check if you are using the ASM API correctly. For this, insert one or more
CheckClassAdapter instances in your class generation or class transformation
chain (in particular, in front of ClassWriter instances). If a CheckClassAdapter
throws an exception, this means that you are not using the ASM API correctly.
Fix these issues first.

If you still have a bug after completing the above steps, describe it below and
make sure to provide detailed instructions on how to reproduce it.
-->

<!--- Provide a brief summary of the issue in the title above -->

### Expected Behavior
<!--- Tell us what should happen. -->

### Current Behavior
<!--- Tell us what happens instead of the expected behavior. -->

### Steps to Reproduce
<!---
Provide a self-contained example as an attached archive. If the source code to
reproduce the issue is very small you can include it here directly (with its
inputs, most likely some binary .class files, as an attached archive).
-->

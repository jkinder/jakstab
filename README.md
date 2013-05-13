Jakstab
=======

Overview
--------

Jakstab is an Abstract Interpretation-based, integrated disassembly
and static analysis framework for designing analyses on executables
and recovering reliable control flow graphs. It is designed to be
adaptable to multiple hardware platforms using customized instruction
decoding and processor specifications. It is written in Java, and in
its current state supports x86 processors and 32-bit Windows PE or
Linux ELF executables.

Jakstab translates machine code to a low level intermediate language
on the fly as it performs data flow analysis on the growing control
flow graph. Data flow information is used to resolve branch targets
and discover new code locations. Other analyses can either be
implemented in Jakstab to run together with the main control flow
reconstruction to improve precision of the disassembly, or they can
work on the resulting preprocessed control flow graph.

The most detailed description of the entire system so far is contained
in Johannes Kinder`s dissertation:

* Johannes Kinder: Static Analysis of x86 Executables. Technische Universität 
Darmstadt, 2010. [PDF](http://nbn-resolving.de/urn:nbn:de:tuda-tuprints-23388)


Running Jakstab
---------------

Jakstab is invoked via the command line, it comes with both a Windows
and a Unix shell script for setting the correct classpath. The package
contains a set of examples for unit testing, you can try it out on
those by running

  `jakstab -m input/helloworld.exe`

for a default Bounded Address Tracking run on the helloworld
executable, or by running

  `jakstab -m input/jumptable.exe --cpa xfi`

for analyzing a jumptable example with Bounded Address Tracking,
forward expression substitution, and interval analysis. It is still a
research prototype, so all interfaces are likely to change with new
versions without further notice. Documentation is still sparse, but
will hopefully improve over time.

Outputs
-------

After finishing analysis, Jakstab creates the following files:

* `filename_jak.asm` - A disassembly with all reachable instructions
* `filename_cfa.dot` - A CFG in the intermediate language, instruction by instruction
* `filename_asmcfg.dot` - A CFG of assembly instructions, in basic blocks

Supported Analyses
------------------

The analyses (CPAs) that should be working correctly are:

* Bounded Address Tracking (x) (see FMCAD'10)
* VPC-lifted Bounded Address Tracking (v) (see WCRE'12)
* Constant Propagation (c)
* Forward Expression Substitution (f)
* Interval Analysis (i)
* K-Set Analysis (k)

Publications
------------

The following publications, sorted chronologically, describe specific
aspects of Jakstab, or applications and extensions of it.

The CAV 2008 tool paper describes an early implementation of Jakstab,
which was based on iterative constant propagation and branch
resolution:

* Johannes Kinder, Helmut Veith. Jakstab: A Static Analysis Platform for
Binaries. In Proceedings of the 20th International Conference on
Computer Aided Verification (CAV 2008), vol. 5123, Lecture Notes in
Computer Science, Springer, July 2008, pp. 423–427.
 
Our VMCAI 2009 paper introduces a generic framework for disassembly
and control flow reconstruction guided by data flow analysis and
defines the theoretical background for Jakstab. The framework is not
fixed in its choice of domain, but allows to combine control flow
reconstruction with any data flow analysis that provides abstract
evaluation of expressions:

* Johannes Kinder, Helmut Veith, Florian Zuleger. An Abstract
Interpretation-Based Framework for Control Flow Reconstruction from
Binaries. In Proceedings of the 10th International Conference on
Verification, Model Checking, and Abstract Interpretation (VMCAI
2009), vol. 5403, Lecture Notes in Computer Science, Springer, January
2009, pp. 214–228.
 
In FMCAD 2010, we give an overview on the Jakstab architecture and
describe Bounded Address Tracking, a practical abstract domain used
for control flow reconstruction and verification of API usage
specifications on device driver binaries:

* Johannes Kinder, Helmut Veith. Precise Static Analysis of Untrusted
Driver Binaries. In Proceedings of the 10th International Conference
on Formal Methods in Computer-Aided Design (FMCAD 2010), October 2010,
pp. 43–50.
 
In our paper at VMCAI 2012, we give a reformulation of control flow
reconstruction using parameterized semantics, and show how it can be
extended to accomodate under-approximations derived from concrete
execution traces. A prototype implementation shows that
under-approximations allow to reconstruct useful CFGs when the
over-approximation would have to conservatively over-approximate
indirect jump targets.

* Johannes Kinder, Dmitry Kravchenko. Alternating Control Flow
Reconstruction. In Proceedings of the 13th International Conference on
Verification, Model Checking, and Abstract Interpretation (VMCAI
2012), vol. 7148, Lecture Notes in Computer Science, Springer, January
2012, pp. 267-282.
 
The WCRE 2012 paper proposes a method for using Jakstab to analyze
binaries that have been protected using virtualization-obfuscation.

* Johannes Kinder. Towards Static Analysis of Virtualization-Obfuscated
Binaries. In Proceedings of the 19th Working Conference on Reverse
Engineering (WCRE 2012), IEEE, October 2012.
 
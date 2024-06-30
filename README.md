### Technion - Israel Institute of Technology
### Course: 	02360651 - Advanced Topics in Software Engineering L+T, Dr. Hila Peleg

## Project based on the paper: [Java Generics Refactoring Tool for Inferring Wildcards](https://yanniss.github.io/variance-oopsla14.pdf) by John Altidor and Yannis Smaragdakis

https://github.com/nikolro/RefactoringTool/assets/134806793/71f6272f-eb3d-4faf-a2c9-de5db5a280e1

### Overview
Developing an IntelliJ IDEA plugin in Java that runs in the background while the programmer writes code. The plugin analyses Java code in real-time to suggest where wildcards can be added to improve the generality of the code.

### Instructions to Run the Benchmarks:
1) Clone the repository to IntelliJ IDEA.
2) Open src/test/java/org.example.refactoringtool/RefactoringToolBenchmarkTests.
3) Right-click and run RefactoringToolBenchmarkTests.
Note: Running each benchmark individually is possible through the file itself.

### To Run the Plugin Itself (For Applying Wildcard Suggestions): 
1) Clone the repository to IntelliJ IDEA.
2) Open the project folder.
3) Wait until it finishes building.
4) Press on the current file tab.
![image](https://github.com/nikolro/RefactoringTool/assets/134806793/10e92db9-228b-4c43-b6ba-31f865e5e0ab)
5) Choose Run Plugin.
6) Run Run Plugin.
   
This will start an IntelliJ IDEA instance where you can create a new Java project and test the plugin.



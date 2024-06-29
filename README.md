### Technion - Israel Institute of Technology
### Course: 	02360651 - Advanced Topics in Software Engineering L+T, Dr. Hila Peleg

## Project based on the paper: [Java Generics Refactoring Tool for Inferring Wildcards](https://yanniss.github.io/variance-oopsla14.pdf) by John Altidor and Yannis Smaragdakis

https://github.com/nikolro/RefactoringTool/assets/134806793/f3b35ed7-d2e3-441a-945b-bce8f93ee1dc

### Overview
Java generics are a powerful feature that improves the safety and maintainability of code by allowing type-safe collections and eliminating the need for casts. 
However, generics can restrict subtyping, making it challenging to create reusable and flexible code. 
This project presents a practical approach to refactoring Java generics by inferring wildcard types, which helps in generalizing type signatures in a way that is both safe and efficient.

### Instructions to run the benchmarks

1) Clone the repository to intellij
2) Open src/test/java/org.example.refactoringtool/RefactoringToolBenchmarkTests
3) RMB run 'RefactoringToolBenchmarkTests'

Note: Running each benchmark individually is possible through the file itself.

To run the plugin itself (so the user will be the one to apply the wildcard suggestions) - 


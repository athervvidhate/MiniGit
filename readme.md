# Final Project: MiniGit

Due: Thursday, June 6th, 11:59 PM  
200 points

## Overview

For the first assignment, you will implement XYZ

**This assignment is an individual assignment.** You may ask Professors/TAs/Tutors for some guidance and help, but you can’t copy code. You may discuss the assignment conceptually with your classmates, including bugs that you ran into and how you fixed them. **However, do not look at or copy code.** This constitutes an Academic Integrity Violation.

### Contents
- [Part 1  - Introduction](#part-1---introduction)
- [Part 2 - Setup](#part-2---setup)
- [Part 3 - Implementation](#part-3---implementation)
- [Submission](#submission)

## **START EARLY!**

## Part 1 - Introduction
[(top)](#contents)

### Introduction to Git

[Link to tutorial](/dne)

### Introduction to Persistence

[Link to tutorial](/dne)

You may have noticed by now that you will need to a way to preserve the state of a program after it finishes running.
The way this is accomplished is through the concept of **persistence**. For example, if your program writes contents to a file,
the file will stay there even after the program finishes running.

First, let's start with basic persistence: writing plain text to a file.

[Writing to files]

[Reading from files]

[Managing directories]

Now, what happens if we wanted to store an entire object in a file? If we wanted to save a linked list, for example, maybe we could go through
each node and save the data on a new line. Then we can reconstruct the original linked list in the future whenever we want by going through the
file line by line and adding each element to a new linked list. However, this is inefficient and can quickly get very complicated for more advanced
data structures. The concept of **serialization** comes to the rescue.

[Explain serializable]

[Explain write object]

[Explain read object]


## Part 2 - Setup
[(top)](#contents)

### Starter Code

Please follow the following steps to get the startercode:
1. Create a new repository on github
    - **Make sure you DO NOT check off the option that says "Add a README file"**
2. Clone this empty repository to your system
3. Navigate to this repository from your terminal
4. Paste the following commands into the terminal:
  ```
  git remote add skeleton https://github.com/kalkulator413/dsc30-project1-startercode.git
  git pull skeleton main
  ```

Then, go to IntelliJ, select the "Open" option, and select the repository you just cloned. **DO NOT 
simply double click on the java files to open them in IntelliJ, you need to open the entire folder.**
Make sure you can run MiniGitTests.

You will be filling in the methods `Repository` class in
the folder named `minigit/`. It may be helpful to design a `Commit` class and use utility
methods inside the `Utils` class.

## Part 3 - Implementation
[(top)](#contents)

Write the following methods inside class `Repository`:
- [init](#init)
- [add](#add)
- [commit](#commit)
- [rm](#rm)
- [log](#log)
- [globalLog](#globallog)
- [find](#find)
- [checkout](#checkout)
- [branch](#branch)
- [rmBranch](#rmbranch)
- [reset](#reset)
- [status](#status)

### init

```
public void init()
```
Description

### add

```
public void add(String[] args)
```
Description

### commit

```
public void commit(String[] args)
```
Description

### rm

```
public void rm(String[] args)
```
Description

### log
```
public void log()
```
Description

### globalLog

```
public void globalLog()
```
Description

### find

```
public void find(String[] args)
```
Description

### checkout
```
public void checkout(String[] args)
```
Description

### branch
```
public void branch(String[] args)
```
Description

### rmBranch
```
public void rmBranch(String[] args)
```
Description

### reset

```
public void reset(String[] args)
```
Description

### status

```
public void status()
```
Description

## Submission
[(top)](#contents)

Files to Submit- minigit/Commit.java
- minigit/Main.java
- minigit/Repository.java
- minigit/Utils.java

To submit your files, you will need to push your code to your private Github repo first, then turn in this PA to GradeScope by selecting Github as the submission method.

After you submit your files to GradeScope, wait for the output of the submission script. If you see the following message after your submission, your submission is successful and you can ignore the warnings in the autograder output:

THIS IS A SUCCESSFUL SUBMISSION. YOUR SCORE WILL BE UPDATED ONCE GRADED.

Otherwise, please fix the error and resubmit your files.

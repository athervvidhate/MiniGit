# Final Project: MiniGit

Due: Thursday, June 6th, 11:59 PM  
200 points

## Overview

For this project you will be implementing a version control system like Git. It has reduced functionality, but it will be able to do 
many of the things that git does, such as staging changes, making commits, creating branches, and more!

**This assignment is an individual assignment.** You may ask Professors/TAs/Tutors for some guidance and help, but you can’t copy code. You may discuss the assignment conceptually with your classmates, including bugs that you ran into and how you fixed them. **However, do not look at or copy code.** This constitutes an Academic Integrity Violation.

### Contents
- [Part 1  - Introduction](#part-1---introduction)
- [Part 2 - Setup](#part-2---setup)
- [Part 3 - Implementation](#part-3---implementation)
- [Submission](#submission)

## **START EARLY! (please)**

## Part 1 - Introduction
[(top)](#contents)

### Introduction to Git

[Link to tutorial (currently nonexistent)](/dne)

### Introduction to Persistence

[Link to tutorial (currently nonexistent)](/dne)

You may have noticed by now that you will need to a way to preserve the state of a program after it finishes running.
The way this is accomplished is through the concept of **persistence**. For example, if your program writes contents to a file,
the file will stay there even after the program finishes running.

First, let's start with basic persistence: writing plain text to a file.

It may be helpful to read up on the methods for the Java File class [here](https://docs.oracle.com/javase/8/docs/api/java/io/File.html).
We will walk you through some of the basic functionality now.

To create a File object, we type in:

```
File f = new File("example.txt");
```

Doing this just creates a reference to the path we want to work with, but it doesn't actually 
create the file. If we wanted to do that, we would type in

```
f.createNewFile();
```

And now if you went through your file manager you would actually see a file called "example.txt". We can use the following helper 
method provided in the `Utils` class to write to the file.

```
Utils.writeContents(f, "DSC 30 is awesome!!");
```

What if we wanted to come back at a later point, maybe after the program ended, and see the contents of the file?
We can do so by calling the helper method:

```
Utils.readContentsAsString(f);
```

Which would then return the string "DSC 30 is awesome!!"

The file class can also manage directories. If we wanted to create a directory, we could do so by using the following methods:
```
File folder = new File("example-folder-location");
folder.mkdir();
```

Now, what happens if we wanted to store an entire object in a file? If we wanted to save a linked list, for example, maybe we could go through
each node and save the data on a new line. Then we can reconstruct the original linked list in the future whenever we want by going through the
file line by line and adding each element to a new linked list. However, this is inefficient and can quickly get very complicated for more advanced
data structures. The concept of **serialization** comes to the rescue.

If you go to the `Commit` class you may notice that it implements the `Serializable` interface.

```
public class Commit implements Serializable {
  ...
```

When a class implements the `Serializable` interface, it allows Java to serialize the object by turning it into a sequence of bytes that can be 
written into a file. Then, these bytes can be used to reconstruct the original object at a later time.

You should look at the `writeObject` and `readObject` methods in `Utils` (as well as all the other helper methods in the file).

It should be noted that serializing a file will recursively serialize all the pointers 
within that file. If you have a bunch of commits pointing to other commits, then serializing a commit will also store every commit before that one which is extremely
inefficient. You should think about how you can get around this.

If you want certain instance variables to not be serialized, you can add the keyword `transient` in front, such as
```
private transient String s;
```
If you initialize the object using `readObject`, then transient fields will be set to `null`, which you may want to be careful about.

It is also recommended to use `Utils.join` to compose paths to files so that we don't have to worry about inconsistencies between forward slashes and backslashes 
on different operating systems.


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

You will be filling in the methods inside the `Repository` class in
the folder named `minigit/`. It may be helpful to design a `Commit` class and use utility
methods inside the `Utils` class.

## Part 3 - Implementation
[(top)](#contents)

Write the following methods inside the `Repository` class:
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

To test your program, you will first want to compile your code by typing the following command into the terminal:
```
javac gitlet/Main.java
```

And then running your program with assorted commands, such as:

```
java gitlet.Main init
```

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

---

Credits: This project was inspired by UC Berkeley's CS61B.
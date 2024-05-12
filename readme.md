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

Throughout this quarter, you have been using Git, but have you thought about how does it work? Git is a version control system where it keeps track of changes that happen in your directory and allows you to transfer across different versions of your working directory. Sounds cool right? But how does it do this? Let's start first by understanding what makes up a git repository.

A git repository is just an area (a hidden folder) where it contains a bunch folders and files that reference the different versions of the working directory (the folder that contains that hidden folder). For simplicity, we will just explain the parts that we are going to implement in this project, but if you want to learn more about how git works you might find this playlist helpful [Link](https://www.youtube.com/playlist?list=PL9lx0DXCC4BNUby5H58y6s2TQVLadV8v7).

The git commands we are interested in exploring are, git init, git status, git add, git commmit, git log, git branch, git checkout, git reset, and git rm.

We will be using these commands when we exlore the repository later. Git init is a command that initializes the git repository. Git status tells us the current status of the repository, whether something was added, deleted, or modified. Git add is a command that sets our changes into an area called INDEX or staging area (we will be using staging area to refer to it) to be ready to commit. Git commit creates an object that stores the current directory with all things that got commited. Git log gives us information about the history of our commits. Git branch is a command that creates a new branch for us to work in. Git checkout makes us change the branch. Git reset moves the pointer of a branch to a certain commit. Git rm removes a file from the directory and from the staging area.

After we have defined the commands for git, let us walk through the changes of a git repository.

First, let's see what is the difference in contents of a git folder and non-git folder.

Create a folder using the following cmd:

```mkdir testingGit```

Change directory to the created folder:

```cd testingGit```

Let us see what is in this folder (including hidden folders) using the following cmd:

```ls -a```

You should get something like this:

```. ..```

Side information, these are basically references to the current directory and the parent directory. That's why when you use ```cd ..``` you go back to the parent folder.

Now let's make this folder a git repository by initializing git:

```git init```

As we expect, the command should initialize the git repository. Now our folder is a git repository and we can verify this by running the status cmd:

```git status```

You should get the following output:

```
On branch main 

No commits yet 

nothing to commit (create/copy files and use "git add" to track)
```

As promised, git status showed us the current status of the repository. It says there is nothing to commit which indicates there is nothing that was changed in the repo yet.

Let's see what changed in our folder by running ```ls -a``` again. You should get the following output:

```.  ..  .git```

As you can see, a folder with the name '.git' has been created. A folder that starts with '.' in it's name is a hidden folder. Let's explore what is in this folder.

```
cd .git
ls
```

You should get the following result:

```
HEAD		description	info		refs
config		hooks		objects
```

Let's breakdown the contents that we need for this project. First, you will find that you have HEAD file.

##### HEAD

Head is a file that just stores the reference to the current branch. You can check it's content by running the following cmd:

MacOS:
```
cat HEAD
```
Windows:
```
type HEAD
```
You should get the following output
```
ref: refs/heads/main
```

As we can see, it is just a path to a certain directory. This directory is what we refer to as branch and we will talk about it later.

Hint: You will need this to change between branches, but you might have a different content of the file.

##### objects

The object folder is where all the commits are stored. To explore this, let's change something in our repository and see how our .git folder would update.

```
cd ..
```
```
echo Hello World > test.txt
```
This should create a file with Hello World as its content. Let's check first the status of the repository:
```
git status
```
You should get the following output:

```
On branch main

No commits yet

Untracked files:
  (use "git add <file>..." to include in what will be committed)
	.DS_Store
	test.txt

nothing added to commit but untracked files present (use "git add" to track)
```
This is basically saying there are files that are in the working directory, but it is not added to the staging area and not commited. Hence, nothing yet changed in the .git folder, because as we said the .git just saves versions of what we tell it to save. To make .git start saving this, we will use the add cmd:

```
git add test.txt
```
```
git status
```
You should get the following output:
```
On branch main

No commits yet

Changes to be committed:
  (use "git rm --cached <file>..." to unstage)
	new file:   test.txt

```

This is telling us we have stuff that are ready to be commited, which means there are things in our staging area. Now let's see what happened in our .git folder:

``` 
cd .git
```
```
ls
```
You should get this output:
```
HEAD		description	index		objects
config		hooks		info		refs
```

As you can see, an index file has been created. This is the staging area. The file stores references of updated files, whether added, deleted or modified, stored in binary code. **Hint**: you will need a similar file or folder to help in implementing add command. Now let's go back and commit our change.
```
cd ..
```
```
git commit -m "added test.txt"
```
You will get an output similar to this:
```
[main (root-commit) 1ef99e1] added test.txt
 1 file changed, 1 insertion(+)
 create mode 100644 test.txt
```
Now let's check the status and then the .git folder:
```
git status
```
Output:
```
On branch main
nothing to commit, working tree clean
```
Which indicates everything is saved in the .git repository.

```
cd .git
```
```
ls
```
Output:
```
COMMIT_EDITMSG	config		hooks		info		objects
HEAD		description	index		logs		refs
```
You will notice that a foler logs has been created along with a another file and folder. We will just talk about logs as we don't need the other newly added folder and file for this project. Let's explore what changed in the folders we care about.

Run:
MacOS:
```
ls -R
```
Windows:
```
tree /f

you can use this if the first one didn't work

dir /s
```

This should show you a tree of everything in the folder. You will notice that the object folder has now a bunch of other folders that contain files with long name. You will also notice that heads folder within the refs folder has a new file called main. This file stores a reference to the most recent commit (if you didn't change it by checkout).

Now we can introduce SHA-1. This is a hashing function that produces a 160-bit hash value for the input. This is how git stores all the references, which is basically just a hash value. You will find that in objects, the folders' names always consist of 2 characters. These are the first 2 characters of the hash value of the commit or the blob object, and the files stored within the folder have the rest of the hash value characters.

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
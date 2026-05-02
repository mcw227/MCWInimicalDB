# Mina Catherine Warner's 
CSE241 Final Project

## Introduction
Welcome to the README for Mina's Inimical database interface implementations. This terminal-based application is intended to function as a customer and managereal interface for a fictional restaurant called *Inimical's.* There are three main interfaces built so far: Customer, Location Manager and Sales Manager (Manager). The details of these interfaces, alongside other helpful or insightful information will be discussed down below.

## Table of Contents
* [Introduction](#introduction)
* [AI Use Disclosure](#ai-use-disclosure)
* [Mock Data Generation](#mock-data-generation)
* [Customer Interface](#customer-interface)
* [Manager Interfaces](#manager-interfaces)
    * [Location Manager](#location-manager)
    * [Sales Manager](#sales-manager)
* [Business Decisions](#business-decisions)
* [Afterword](#afterword)
    

## AI Use Disclosure
&emsp;Generally, I do avoid (to the best of my abilities.. you can't even search these days!) using any kind of AI with my projects because I feel it defeats the point of taking the course. You haven't really learned anything if you have to rely on a tool to do the job for you.<br>
&emsp;BUT! I think that AI *should* be used as a powerful tool (alongside personal effort) to speed up project workflow and debugging. In this case, by "workflow", I mean parts of the project that are either strictly aesthetic (list pager class), unrelated to the content of this class (I forgot how method overriding vs overloading works... so I had to ask Gemini what the difference was...), or "outside" the scope of this class (e.g.: what are & how to write compound triggers & why you may need them; How to fetch generated ID from an update query in java; What is a good regex expression to capture emails?). <br>
&emsp;That being said, there should be only one example of AI code that was copy-pasted into any of my files. That being Pager.java class. That class was originally written by hand (you may be able to see its many iterations through my github repo), but never worked quite right. I spent about three hours on it throughout the project, but I really just could not get it to work consistently. In the end, I decided my time was better spent elsewhere in the project and on code actually relevant to the course, so I resorted to Gemini for debugging and it told me that my math was not only unnecessarily complex, but also logically flawed... and then gave me a much shorter solution that worked 100% of the time.
<br>&emsp;Unfortunately I don't have access to the exact prompt & response anymore, but I believe it was something like this:<br>

1. "Why doesn't my paged list next function work?"
2. (Why my function is flawed, followed by working solution)
3. "Can you help me with my previous function too?"
4. (working previous function)

&emsp; As for the other uses, these were the approximate questions I asked:
1. "What does [insert vaguely worded Oracle SQL error here] error mean?"
1. "What is the difference between method overriding and overloading super/subclass methods in Java?"
1. "If my ID is created with [insert automatic ID creation syntax here], how can I obtain the generated ID upon insertion using JDBC?"
1. "Can you generate a simple regex expression to verify emails?"

&emsp; This concludes my AI usage disclosure.

## Mock Data Generation
&emsp;Mock data was generated in part by "Mockaro" for customers, phone numbers and credit cards. I then got lazy... the rest of the data was generated using the python library called "faker." That being said, you may be wondering why certain numbers are much larger than they should be.
&emsp;For example, tax at many locations exceeds 50%! Some items cost an insane amount of money! The reason for this is that I wanted to use large numbers so that it was easy to see things such as sales tax take effect as I was testing. Probably not the best method for that... but it led to some pretty funny pricing...

&emsp;Within the "DBSetup" folder, there are three files:
1. table_dropper.sql (self explanatory)
2. table_maker.sql (self explanatory)
3. table_populator.py

&emsp;The only one of note is table_populator.py which can be used to generate mock data that fits within the expectations of the CLI.
It is some *really* dodgy code, but it works! I would like to apologize to all of my comp. sci. professors for creating such a wretched collection of bad coding practices.

## Customer Interface

## Manager Interfaces

### Location Manager

### Sales Manager

## Business Decisions

## Afterword
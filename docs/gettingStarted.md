# Getting Started

## Pre-requisites

To use HubTurbo, you'll need:

- A [GitHub account](https://github.com/login)
- [Java 8u40](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later
- [Google Chrome](http://www.google.com/chrome/)

## Installation

Once you have the above, grab HubTurbo from [here](https://github.com/HubTurbo/HubTurbo/releases/latest) and run it.

When HubTurbo runs for the first time, you'll be presented with a login dialog similar to the one shown below. 
Provide the URL of one of the repositories you want to access and your GitHub account details.

<img src="images/gettingStarted/exampleLoginDialog.png" width="602">

When you click the `Sign in` button, HubTurbo will download the issues from the repo you specified and present to you 
 the typical HubTurbo interface.
 
 ![](images/gettingStarted/uiMainComponentsScreenshot.png?raw=true)
  
Before you start using HubTurbo features, let us take a few minutes to learn basic concepts of the HubTurbo UI.

## UI Basics

HubTurbo's interface has two parts: The **panel view** and the **browser view**.

![](images/gettingStarted/uiMainComponentsBreakdown.png?raw=true)

### Panel View

<img src="images/gettingStarted/panelViewBreakdown.png" width="600">

The **panel view** displays a **board** which contains a collection of **panels**. 

<img src="images/gettingStarted/panelExplanation.png" width="600">

A panel provides a 'filtered view' into the issue tracker by displaying a list of 
**issue cards** that match the **filter** you specify at the top of the panel. 
You can have any number of panels in a board, to get a side-by-side view of all issues you are interested in. 

For example, a board can contain three panels showing,

1. all open issues assigned to you from projects *alpha*, *beta*, and *gamma* (yes, a panel can show issues from multiple projects)
2. all issues of project *beta* updated within the last 24 hours  
3. all open issues allocated to the next upcoming milestone in *project alpha*

Boards can be named, saved, and reloaded later. For example you can have one board called 'work projects' to interact 
with work related projects and another board called 'pet projects' to interact with your hobby projects. 

The **default repository** dropdown allows you to specify what HubTurbo considers to be the *default repository*. 
HubTurbo can be viewing many repositories at once; if you do not specify a repository, 
HubTurbo assumes you meant to work with the default repository. 

### Browser View

<img src="images/gettingStarted/browserViewExplanation.png" width="800">

The **browser view** is a Chrome Window controlled by HubTurbo, and it shows GitHub pages as needed by the panel view; 
for example, clicking on an issue in a panel will make the browser view to navigate to the corresponding issue page on GitHub.

## HubTurbo Workflow

The general workflow is to switch back and forth between the two windows, 
staying in the panel view as much as possible to take advantage of extra productivity features 
provided by HubTurbo (e.g. keyboard shortcuts), and going to the browser view for working with issue details when necessary.



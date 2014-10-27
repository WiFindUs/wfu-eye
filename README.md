# WiFindUs Eye Platform
Documentation for WiFindUs's Eye Platform.

## Authors
[Mark 'marzer' Gillard \(gill0235\)](kttp://www.marzersoft.com/)  
Hussein Al Hammad  
Mitchell Templeton

## Adding an java-event-handled column to a monitored MySQL table
Many of the variables used by the classes in this project are Java mirrors of MySQL constructs, and fire events appropriately when they're changed in the backing database. This is triggered by a monitoring thread that polls the database every few seconds. As useful as this is, in the event a column needs to be added it can be a pretty frustrating process with a lot of points of failure (it's easy to forget something).  

Following these steps should help minimize this problem.

1. Append the database initialization script, [wfu_init.sql](https://github.com/WiFindUs/wfu-eye/blob/master/wfu_init.sql), with the new column(s)
2. Add the new columns to the appropriate table retrieval function in the [EyeMySQLConnection](https://github.com/WiFindUs/wfu-eye/blob/master/src/wifindus/eye/EyeMySQLConnection.java)
3. Add a member variable to the relevant class
4. Create any necessary accessors
5. Add new handlers to the relevant event listener interface (e.g. [IncidentEventListener](https://github.com/WiFindUs/wfu-eye/blob/master/src/wifindus/eye/IncidentEventListener.java))
6. Connect your new event in the target class's `mapEvents()`
7. Add updating logic to the target class's `updateFromMySQL()`
8. Add mutators to trigger changes in response to database updates
9. Link any key-based values (object references etc) up in `EyeApplication.MySQLUpdateWorker`
10. Make any necessary changes to associated `db_...()` functions in `EyeApplication`

## Code/Style Guidelines

### Use 'self-documenting' method and variable names
Sometimes names make sense in context (i.e. when reading the code of the class they're defined in), but when accessed from outside, or interpreted by another programmer, it can get a bit hazy.  

Thus, always like to try and use names that are *self-documenting*; anyone reading it out-of-context should be able to say something like *"Ah, `newLocation`, I guess that's the variable which holds the new location information"*.

Of course there are instances where this isn't practical (long function names can be pretty annoying), but in these situations a reasonably fleshed-out `@javadoc` block will suffice.

### Provide ongoing commentary with single-line comments
Even though, in a perfect world, names would be self-documenting and code flow is easy to follow and understand, sometimes a bit of extra help is nice. Just a few single-line comments here and there, particularly during loops, will help keep the code maintainable, e.g.:
```java  
//parse command line arguments for config parameters
Debugger.i("Parsing command line arguments for config files...");
List<File> configFiles = new ArrayList<>();
for (int i = 0; i < args.length-1; i++)
{
	//skip arguments that do not begin with a dash
	if (!args[i].substring(0, 1).equals("-"))
		continue;
	
	//find config file arguments
	if (args[i].equalsIgnoreCase("-conf"))
	{
		configFiles.add(new File(args[++i]));
		Debugger.i("Found '"+args[i]+"'.");
		continue;
	}
}
```


### Use Javadocs for `public` and `protected` methods/constants
Ordinarily normal comments are fine, but Eclipse makes javadoc generation really easy by providing automatic generation of tags when you type `/**` and press `ENTER`.

Apart from allowing for actual HTML javadoc generation, this is a good habit to get into for `public` and `protected` methods, constants, inner classes, etc. because it works within Eclipse (e.g. when you hover the mouse over something).

### Group related statements, separate with whitespace
Sometimes a function is just a massive series of simple statements, one after the other, with no control structures, loops, etc. Constructors of UI components are a good example of this. It can be very hard to follow logic and isolate issues when you wall of text; try wherever possible to separate statements with related purposes into groups using whitespace (and comments where necessary). For example:
```java  
// buttons for creating incidents & locating users
newIncidentButton = new JButton("New Incident");
newIncidentButton.setIcon(newIncidentLogo);
newIncidentButton.setBackground(Color.white);
    //...more

locateOnMapButton = new JButton("Locate on map");
locateOnMapButton.setIcon(showOnMapLogo);
locateOnMapButton.setBackground(Color.white);
    //...more
```

### Inline for fun and profit
Wherever possible, and where you can do so without significantly sacrificing readability, reduce a series of operations into one inline statement, e.g.:
```java  
//this:
Double accuracy = entry.getValue().get("accuracy") == null ? null : (Double)entry.getValue().get("accuracy");

//...instead of this:
Double accuracy;
if (entry.getValue().get("accuracy") == null)
    accuracy = null;
else
    accuracy = entry.getValue().get("accuracy");
```

### Make use of TODO's  
If you plan to return to or improve something later, use a `//TODO:` comment; this will make Eclipse track it in the Tasks panel (`Window` &rarr; `Show View` &rarr; `Tasks`). This is a really good way of reminding yourself, as well as providing a quick reference for others of what work still needs doing througout the project.  

### Be conservative with Interface expansion  
When implementing an interface, Ecplise will provide you with the option to automatically generate methods for all the signatures it declares. While this is very handy, it does mean your code can become bloated, e.g.:  
```java  
@Override
public void incidentArchived(Incident incident)
{
	//TODO: auto-implemented method stub
}

@Override
public void incidentAssignedDevice(Incident incident, Device device)
{
	//TODO: auto-implemented method stub
}
```
Often times you'll only need one or two methods from an listener interface that may declare a great many of them (e.g. Swing's `WindowListener`). In this case, you should collapse the methods you do not intend to use down to single lines, and remove the `//TODO:` statements, like this:
```java  
//WindowListener
@Override public void windowOpened(WindowEvent e) { }
@Override public void windowClosed(WindowEvent e) { }
@Override public void windowIconified(WindowEvent e) { }
@Override public void windowDeiconified(WindowEvent e) { }
@Override public void windowActivated(WindowEvent e) { }
@Override public void windowDeactivated(WindowEvent e) { }
```

### Avoid creating new objects where possible
Underneath the hood the Eye platform is constantly monitoring an SQL database and triggering an event system when various types of events are detected. Since, when we go live, this may end up being triggered many times per second, you should avoid creating new objects where possible (particularly any that subscribe to events), as you may be inadvertently introducing memory leaks (e.g. creating new DevicePanels for sorting, rather than rearranging
the existing ones).

### Avoid using static classes where possible
Any components you create will very likely have no need to be static. Even if, at the moment, it appears as you'd only ever need one instance of an object, down the track this may change and it can be very complicated to convert a large class from a static 'singleton' pattern to a instance-based one.

#### *"But Mark, you've done this for `EyeApplication` and `Debugger`, you hypocrite!"*
I know, I know. However, these are justifiable; there's only ever going to be one `EyeApplication` per execution of the program, and the `Debugger` internally manages logfile output, which doesn't make sense to be running more than once.

Consider MapFrame and MapImagePanel; you can easily imagine a scenario where we may want to have more than one MapFrame open at once (perhaps the dispatcher would like to focus on more than one area, if we provide the ability to zoom?) - making these static prevents this from being done easily.

Having said that, it may well be that you're not really all that clear on how `static` works, which is fair enough (`volatile` and `transient`, too). If this is the case, feel free to let me know and I'll try my best to help :)

### Take heed of the warnings Eclipse generates
Sometimes Eclipse can be a bit anal-retentive. It likes to throw lots of little yellow triangles at you, and make you feel like *"...can you just fuck off, or what?"*. It can get annoying. they're there for a reason, though. Before each commit you make, ensure you've resolved as many of these warnings as possible.

One of the most frequent causes of these warnings is simply unused Java imports; you can very easily fix this project-wide: `Right-click the project root` &rarr; `Source` &rarr; `Organize Imports`. `Ctrl+Shift+O` also performs this function.  
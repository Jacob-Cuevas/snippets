Java class used to print differents types of tickets in a Zebra printer with Bluetooth conectivity. We can print a sale note, a report of products in stock and a sales clearance ticket for the day. <br>
This class use the SDK from Zebra to make a successful integration of the printer interface with the Android app. As well, was necessary include the printer language ZPL to code all the content of the different types of tickets to print.<br>
All the operations performed by the class are the following:<br>
- Create a connection between a Android device and a Zebra printer using Bluetooth, validating the status from connection and the disponibility from printer.<br>
- Get all information from SQLite database that is necessary include in the sale note, such as quantity of products sold or the description of each product in stock.<br>
- Code the content of the ticket in ZPL language, concatenating the value, names, quantities, amounts for each product in the sale order.
- Print the resulting ticket on the Zebra printer with a specific format.
To do the amount conversions is used the Java class "FuncionesMonetarias.java".
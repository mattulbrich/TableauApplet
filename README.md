# Tableau Applet

*a Java implementation of an interative tableau first order theorem prover.*

It is mainly used for teaching, but can also prove simple FOL formulas automatically.

Several options can be set when running to activate/deactivate features.

## En/Dis-abling Features

The following parameters can be set when calling the applet from
the command line.

Option | default | Meaning
------ | ------- | -------
`-Dtablet.showancestor=[true|false]` | true | list the ancestor node in the display 
`-Dtablet.allowautorun=[true|false]` | false | allow automatic proving [switched off for didactic reasons]
`-Dtablet.absolutetex=[true|false]`  | ? | position TeX export absolute or relative.
`-Dtablet.fontsize=<number>` | ? | font size to be used to show nodes
`-Dtablet.allowunification=[true|false]` | false | a llow the unification of formulas when drag'n'dropping nodes. [switched off for didactic reasons] 

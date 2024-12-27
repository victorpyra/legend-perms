### Allgemeine Informationen:

---

*Projekt aus: Dezember 2023* 
- Damals für eine Bewerbung in einem größeren Projekt [PlayLegend](https://playlegend.net/) genutzt. Heutzutage, um sich an der internationalen Hochschule und deren Praxispartner vorzustellen.

Hoffentlich sind die wichtigsten Funtkionen hieraus ersichtlich, ansonsten kann man auch einfach in den Quellcode nachschauen. 
Basis des Projekts ist die [Paper-API](https://docs.papermc.io/paper/dev), welche als "Framework" für Minecraft-Plugins nutzt.



---

## Wichtigsten Befehle:

---

### Nutzerbefehle:

```
/perms user <name> add <permission> - fügt dem Spieler eine Berechtigung hinzu
/perms user <name> remove <permission> - entfernt dem Spieler eine Berechtigung
/perms user <name> info - zeigt die Berechtigungen des Spielers an
/perms user <name> group add <group> [duration] - fügt dem Spieler eine Gruppe hinzu
/perms user <name> group remove <group> - entfernt dem Spieler eine Gruppe
```

### Schilderbefehle:
```
/perms sign set - setzt ein Permissions-Schild (man muss auf ein leeres Schild schauen)
/perms sign remove - entfernt ein Permissions-Schild
```

### Gruppenbefehle:

```
/perms groups - zeigt eine Auflistung aller Gruppen an
/perms group <name> create - erstellt eine Gruppe 
/perms group <name> delete - löscht eine Gruppe
/perms group <name> add <permission> - fügt der Gruppe eine Berechtigung hinzu
/perms group <name> remove <permission> - entfernt der Gruppe eine Berechtigung
/perms group <name> info - zeigt Informationen über die Gruppe an
/perms group <name> default <true/false> - setzt die Gruppe als Standardgruppe
/perms group <name> prefix <prefix> - setzt den Prefix der Gruppe
/perms group <name> suffix <suffix> - setzt den Suffix der Gruppe
/perms group <name> weight <weight> - setzt das Gewicht der Gruppe - je höher, desto höher die Priorität
```

Allgutig


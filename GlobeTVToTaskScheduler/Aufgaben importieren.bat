schtasks /delete /TN "Meine Aufgaben\TV-Aufzeichnungen\PC aufwecken" /F
schtasks /delete /TN "Meine Aufgaben\TV-Aufzeichnungen\Globe TV starten" /F
schtasks /delete /TN "Meine Aufgaben\TV-Aufzeichnungen\Globe TV beenden" /F
schtasks /delete /TN "Meine Aufgaben\TV-Aufzeichnungen\PC in Ruhezustand versetzen" /F
schtasks /delete /TN "Meine Aufgaben\TV-Aufzeichnungen\PC herunterfahren" /F

schtasks /create /XML "C:\Windows\System32\Tasks\Meine Aufgaben\TV-Aufzeichnungen\PC aufwecken.xml" /TN "Meine Aufgaben\TV-Aufzeichnungen\PC aufwecken"
schtasks /create /XML "C:\Windows\System32\Tasks\Meine Aufgaben\TV-Aufzeichnungen\Globe TV starten.xml" /TN "Meine Aufgaben\TV-Aufzeichnungen\Globe TV starten"
schtasks /create /XML "C:\Windows\System32\Tasks\Meine Aufgaben\TV-Aufzeichnungen\Globe TV beenden.xml" /TN "Meine Aufgaben\TV-Aufzeichnungen\Globe TV beenden"
schtasks /create /XML "C:\Windows\System32\Tasks\Meine Aufgaben\TV-Aufzeichnungen\PC in Ruhezustand versetzen.xml" /TN "Meine Aufgaben\TV-Aufzeichnungen\PC in Ruhezustand versetzen"
schtasks /create /XML "C:\Windows\System32\Tasks\Meine Aufgaben\TV-Aufzeichnungen\PC herunterfahren.xml" /TN "Meine Aufgaben\TV-Aufzeichnungen\PC herunterfahren"
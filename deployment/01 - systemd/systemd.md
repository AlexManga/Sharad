# Redémarrer automatiquement l'application au redémarrage du serveur

Dans `/lib/systemd/system/`, créer un script, disons `sharad.service` root:root -rw-r--r-- :

```
[Unit]
Description=Sharad application server

[Service]
ExecStart=/usr/local/bin/java -jar /home/sharad/sharad-back.jar
Restart=on-failure
RestartPreventExitStatus=255
Type=simple
PIDFile=/home/sharad/logs/sharad.pid
User=sharad
WorkingDirectory=/home/sharad

[Install]
WantedBy=multi-user.target
```

Note : Il faut que le jar à lancer se nomme `sharad-back.jar`. Si on souhaite garder des noms contenant les versions, il faudrait mettre en place un `update-alternatives` probablement

Pour un redémarrage automatique : `sudo systemctl enable sharad`

#!/bin/python3
import mysql.connector
import os 

ident = os.system("ls | grep allports >> ls.tmp")
#print(ident)

if ident == 0:
    try:
        with open("allports","r",encoding="utf-8") as archivo:
            
            contenido = archivo.read()
            #print(contenido)

            puertos = os.system("cat allports | grep -oP '\d{1,5}/open' | awk '{print $allports}' FS='/' | xargs | tr ' ' ',' >> puerto.tmp")

            ip = os.system("cat allports | grep -oP '\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}' | sort -u | head -n 1 >> ip.tmp")

            with open("puerto.tmp","r",encoding="utf-8") as aPuerto:
                with open("ip.tmp", "r", encoding="utf-8") as aIp:

                #print(aPuerto.read())
                #print(aIp.read())
                    consulta = "INSERT INTO direccionesIP (ip, puertos) VALUES (%s, %s)"
                    valores = (aIp.read(),aPuerto.read())
                    #print(consulta,valores)

                    mydb = mysql.connector.connect(
                    host="localhost",
                    user="angel",
                    password="angel",
                    database="proyecto"
                    )
        
                    mycursor = mydb.cursor()

                    mycursor.execute(consulta,valores)
                    mydb.commit()

                    archivo.close()
                    mydb.close()

                    os.system("rm allports ip.tmp puerto.tmp ls.tmp -f")
                    print("Datos almacenados en la DB ")

    except:
        print("Error al procesar el archivo de registro")
else:
    print("El archivo de nmap no ha sido encontrado ")

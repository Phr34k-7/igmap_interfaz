# igmap_interfaz
Proyecto de interfaz grafica para el escaner de puertos nmap

Descripción del Proyecto
IGMAP es una interfaz gráfica de usuario (GUI) para el escáner de red Nmap, diseñada para simplificar y visualizar el proceso de auditoría de seguridad y descubrimiento de redes. El objetivo principal de este proyecto es ofrecer una herramienta intuitiva que permita a los usuarios, tanto principiantes como avanzados, ejecutar comandos de Nmap, analizar los resultados y generar reportes de manera eficiente.

Características Principales
Escaneo de Puertos: Permite ejecutar escaneos de puertos básicos y avanzados sobre una dirección IP o dominio.

Visualización de Resultados: Los resultados de Nmap se muestran en diferentes formatos (salida de texto sin procesar, tabla de puertos y servicios, topología de red) para una comprensión más fácil.

Generación de Reportes: La herramienta puede crear reportes en formato PDF con la información clave del escaneo, ideal para documentación y presentación de hallazgos.

Gestión del Historial: Guarda un registro de los escaneos realizados, permitiendo a los usuarios revisarlos y analizarlos en cualquier momento.

Base de Datos Local: Almacena los registros de los escaneos en una base de datos local (MySQL) para persistencia y consulta.

Modo Oscuro: Incluye una opción para cambiar a un tema de modo oscuro, mejorando la experiencia de usuario en entornos de baja luminosidad.

Interfaz Gráfica Intuitiva: Diseñada con una navegación clara y botones de acción rápidos para mejorar la usabilidad.

Tecnologías Utilizadas
Java: El proyecto está completamente construido en Java, aprovechando la versatilidad de la plataforma para la interfaz de usuario y la lógica del programa.

Swing: Se utilizó la biblioteca Swing para crear la interfaz gráfica, incluyendo elementos como JFrame, JPanel, JButton, y JTabbedPane.

MySQL Connector/J: Se emplea para la conexión a la base de datos MySQL, permitiendo la gestión y almacenamiento de los registros de escaneo.

iText 7: Se usó para la generación de reportes en PDF, lo que permite crear documentos estructurados con los resultados del escaneo.

JGraphX: Para la visualización de la topología de red, proporcionando una representación gráfica de las rutas de los paquetes.


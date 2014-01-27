#!/usr/bin/python
# coding: utf-8

from bluetooth import *

MODO_TEXTO          = 0
MODO_ESPECTROSCOPIO = 1
MODO_BEATBOX        = 2
DEBUG               = False

class BluetoothServer:
    def __init__(self):
        # Modo por defecto
        self.modo = MODO_TEXTO

        # Inicializar el socket Bluetooth y ponerlo a la escucha
        self.socket_servidor = BluetoothSocket(RFCOMM)
        self.socket_servidor.bind(("", PORT_ANY))
        self.socket_servidor.listen(1)

        # Obtener el puerto y setear la UUID del servicio
        self.puerto = self.socket_servidor.getsockname()[1]
        # El servicio RFCOMM tiene esta UUID
        self.uuid = "00001101-0000-1000-8000-00805f9b34fb"

        # Anunciar el servicio
        advertise_service( self.socket_servidor, "SampleServer",
            service_id = self.uuid,
            service_classes = [ self.uuid, SERIAL_PORT_CLASS ],
            profiles = [ SERIAL_PORT_PROFILE ], 
            # protocols = [ OBEX_UUID ] 
        )

    def iniciar(self):
        self.debug("Esperando una conexión en el canal RFCOMM, puerto %d" % self.puerto)
        try:
            self.socket_cliente, self.client_info = self.socket_servidor.accept()
        except KeyboardInterrupt:
            self.debug("Cancelado.")
            self.socket_servidor.close()
            exit(0)
        self.debug("Aceptada la conexión de {}".format(self.client_info))
        self.recibir()

    def tratar_datos(self, data):
        if self.modo == MODO_TEXTO:
            self.debug("Texto: [%s]" % data)
        elif self.modo == MODO_ESPECTROSCOPIO:
            self.debug("Espectroscopio: [%s]" % data)
        elif self.modo == MODO_BEATBOX:
            self.debug("Beatbox: [%s]" % data)

        if __name__ != "__main__":
            self.avisar_data(data)

    def recibir(self):
        try:
            while True:
                data = self.socket_cliente.recv(1024)
                #if len(data) == 0: break
                
                if data == "\\T":
                    self.debug("[Modo Texto]")
                    self.modo = MODO_TEXTO
                elif data == "\\S":
                    self.debug("[Modo Espectroscopio]")
                    self.modo = MODO_ESPECTROSCOPIO
                elif data == "\\B":
                    self.debug("[Modo Beatbox]")
                    self.modo = MODO_BEATBOX
                else:
                    self.tratar_datos(data)

                if data[0] == "\\" and __name__ != "__main__":
                    self.avisar_modo(self.modo)

                self.debug("received [%s]" % data)

        except IOError:
            self.desconectar()
        except KeyboardInterrupt:
                self.debug("Cierre de sesión")
                self.desconectar()

    def desconectar(self):
        self.debug("Desconectando...")
        if hasattr(self, "socket_cliente"):
            self.socket_cliente.close()
        self.socket_servidor.close()
        self.debug("Desconectado")

    def set_escuchador(self, escuchador):
        self.debug("Escuchador: %s" % escuchador)
        self.escuchador = escuchador

    def avisar_modo(self, modo):
        self.debug("avisando a escuchador de modo")
        self.escuchador.on_mode_changed(modo)

    def avisar_data(self, data):
        self.debug("avisando a escuchador de modo")
        self.escuchador.on_data_received(data)

    def debug(self, msg):
        if DEBUG:
            print "[bluetoothserver] %s" % msg
        if hasattr(self, 'escuchador'):
            self.escuchador.on_status_changed(msg)

if __name__ == "__main__":
    server = BluetoothServer()
    server.iniciar()
    print "Server finalizado"
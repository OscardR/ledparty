#!/usr/bin/env python
# coding: utf-8

import gtk
import pygtk
import gobject
from gtk import Window
from gtk import Notebook
from gtk import Label
from threading import Thread
from bluetoothserver import BluetoothServer, MODO_TEXTO, MODO_ESPECTROSCOPIO, MODO_BEATBOX

gobject.threads_init()

class LEDPartyServer:
    def __init__(self):
        print "LEDPartyServer iniciado"
        
        self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)
        w = self.window
        w.set_title("LEDParty ServerSIM")
        w.set_default_size(300, 300)
        w.connect("destroy", self.destroy)

        self.notebook = gtk.Notebook()
        nb = self.notebook
        nb.set_tab_pos(gtk.POS_TOP)
        nb.connect("switch-page", self.on_page_changed)

        h = gtk.HBox()
        self.add_page("Texto", h)
        self.add_page("Espectroscopio", h)
        self.add_page("BeatBox", h)
        w.add(nb)

        self.show_all()

    def start_bluetooth(self):
        self.bluetooth_server = BluetoothServer()
        self.bluetooth_server.set_escuchador(self)
        self.bluetooth_server.iniciar()
        
    def main(self):
        self.t = Thread(target = self.start_bluetooth)
        self.t.daemon = True
        self.t.start()
        
        gtk.main()

    def destroy(self, widget):
        self.bluetooth_server.desconectar()
        gtk.main_quit()

    def add_page(self, title, frame):

        self.hbox = gtk.HBox()
        h = self.hbox
        h.show()
        l = gtk.Label("Texto recibido")
        l.show()
        h.add(l)
        self.label_title = Label(title)
        l = self.label_title
        l.show()

        self.notebook.append_page(h, l)

    def show_all(self):
        print "show_all"
        self.window.show()
        self.notebook.show()

    def on_page_changed(self, notebook, page, page_num):
        print "page_changed llamado para página ", page_num
        selected_tab_child = notebook.get_nth_page(page_num)
        selected_tab_label = notebook.get_tab_label(selected_tab_child)
        print "tengo: ", selected_tab_label.get_text()

    def on_mode_changed(self, mode):
        print "[on_mode_changed: %d]" % mode
        self.notebook.set_current_page(mode)

    def on_data_received(self, data):
        print "[app] data received: %s" % data

if __name__ == "__main__":
    app = LEDPartyServer()
    try:
        app.main()
    except KeyboardInterrupt:
        app.destroy(app.window)

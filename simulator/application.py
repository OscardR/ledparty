#!/usr/bin/env python
# coding: utf-8

import gtk
import pygtk
import gobject
import pango
from gtk import Window
from gtk import Notebook
from gtk import Label
from threading import Thread
from bluetoothserver import BluetoothServer, MODO_TEXTO, MODO_ESPECTROSCOPIO, MODO_BEATBOX
DEBUG = True
gobject.threads_init()

class LEDPartyServer:
    def __init__(self):
        self.debug("LEDPartyServer iniciado")
        self.mode = MODO_TEXTO
        
        self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)
        w = self.window
        w.set_title("LEDParty ServerSIM")
        w.set_default_size(300, 300)
        w.connect("destroy", self.destroy)

        self.vbox = gtk.VBox(spacing=3)

        # Notebook
        self.notebook = gtk.Notebook()
        nb = self.notebook
        nb.set_tab_pos(gtk.POS_TOP)
        nb.connect("switch-page", self.on_page_changed)

        # Tab del modo Texto
        hTexto = gtk.HBox()
        hTexto.show()
        self.lblText = gtk.Label(u"\u2014")
        self.lblText.modify_font(pango.FontDescription("sans 32"))
        self.lblText.show()
        hTexto.add(self.lblText)

        # Tab del modo Espectro
        hEspectro = gtk.HBox()
        hEspectro.show()

        # Tab del modo Beatbox
        hBeatbox = gtk.HBox()
        hBeatbox.show()
        self.beatboxArea = gtk.DrawingArea()
        self.beatboxArea.set_size_request(300, 300)
        self.beatboxArea.show()
        hBeatbox.add(self.beatboxArea)

        self.add_page("Texto", hTexto)
        self.add_page("Espectroscopio", hEspectro)
        self.add_page("BeatBox", hBeatbox)
        self.vbox.pack_start(nb)

        # Status bar
        self.bar = gtk.Statusbar()
        self.bar.push(0, 'Listo')
        self.tasks = 0
        self.vbox.pack_start(self.bar, 0)

        w.add(self.vbox)
        w.show_all()

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

        l = Label(title)
        l.show()

        self.notebook.append_page(frame, l)

    def show_all(self):
        self.debug("show_all")
        self.window.show()
        self.notebook.show()

    def on_page_changed(self, notebook, page, page_num):
        self.debug("page_changed llamado para página %d" % page_num)
        selected_tab_child = notebook.get_nth_page(page_num)
        selected_tab_label = notebook.get_tab_label(selected_tab_child)
        self.debug("tengo: %s" % selected_tab_label.get_text())

    def on_mode_changed(self, mode):
        self.debug("[on_mode_changed: %d]" % mode)
        self.mode = mode
        self.notebook.set_current_page(self.mode)

    def on_data_received(self, data):
        self.debug("data received: %s" % data)
        if self.mode == MODO_TEXTO:
            self.lblText.set_text(data)
        elif self.mode == MODO_ESPECTROSCOPIO:
            pass
        elif self.mode == MODO_BEATBOX:
            data = float(data)
            drawable = self.beatboxArea.get_window()
            gc = drawable.new_gc()
            gc.set_foreground(gtk.gdk.Color(
                data * 65535, 
                data * 65535, 
                data * 65535))
            drawable.draw_rectangle(
                gc, True, 
                10, 10, 
                280, 280)
        else:
            self.debug("Modo incorrecto")

    def on_status_changed(self, msg):
        if hasattr(self, 'bar'):
            self.bar.push(0, msg)
            
    def debug(self, msg):
        if DEBUG:
            print "[application] %s" % msg

if __name__ == "__main__":
    app = LEDPartyServer()
    try:
        app.main()
    except KeyboardInterrupt:
        app.destroy(app.window)

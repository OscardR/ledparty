#!/usr/bin/env python
# coding: utf-8

from gi.repository import Gtk, GObject, Pango
from threading import Thread
import cairo
import math
import random
from bluetoothserver import BluetoothServer, MODO_TEXTO, MODO_ESPECTROSCOPIO, MODO_BEATBOX

DEBUG = False
GObject.threads_init()

class LEDPartyServer(Gtk.Window):
    def __init__(self):
        Gtk.Window.__init__(self, title="LEDParty ServerSIM")
        self.set_default_size(640, 480)
        self.connect("destroy", self.destroy)

        self.mode = MODO_TEXTO
        self.last_beat = 0
        self.debug("LEDPartyServer iniciado")
        
        # Layout vertical
        self.vbox = Gtk.VBox(spacing=3)

        # Notebook para los tabs
        self.notebook = Gtk.Notebook()
        nb = self.notebook
        nb.set_tab_pos(Gtk.PositionType.TOP)
        nb.connect("switch-page", self.on_page_changed)

        # Tab del modo Texto
        hTexto = Gtk.HBox()
        hTexto.show()
        self.lblText = Gtk.Label(u"<vacío>")
        self.lblText.modify_font(Pango.font_description_from_string("sans 32"))
        self.lblText.show()
        hTexto.add(self.lblText)

        # Tab del modo Espectro
        hEspectro = Gtk.HBox()
        hEspectro.show()
        self.spectrumArea = Gtk.DrawingArea()
        self.spectrumArea.connect('draw', self.on_spectrum_draw)
        self.spectrumArea.show()
        hEspectro.add(self.spectrumArea)

        # Tab del modo Beatbox
        hBeatbox = Gtk.HBox()
        hBeatbox.show()
        self.beatboxArea = Gtk.DrawingArea()
        self.beatboxArea.connect('draw', self.on_beatbox_draw)
        self.beatboxArea.show()
        hBeatbox.add(self.beatboxArea)

        self.add_page("Texto", hTexto)
        self.add_page("Espectroscopio", hEspectro)
        self.add_page("BeatBox", hBeatbox)
        self.vbox.pack_start(nb, True, True, 0)

        # Status bar
        self.bar = Gtk.Statusbar()
        self.bar.push(0, 'Listo')
        self.tasks = 0
        self.vbox.pack_start(self.bar, False, True, 0)

        self.beatbox_timer = GObject.timeout_add(10, self.on_update_beatbox)
        self.spectrum_timer = GObject.timeout_add(10, self.on_update_spectrum)
        print self.beatbox_timer

        # Añadir Layout
        self.add(self.vbox)
        self.show_all()

    def on_update_beatbox(self):
        self.beatboxArea.queue_draw()
        return True

    def on_update_spectrum(self):
        self.last_array = [random.random() for i in range(16)]
        self.spectrumArea.queue_draw()
        return True

    def start_bluetooth(self):
        self.bluetooth_server = BluetoothServer()
        self.bluetooth_server.set_escuchador(self)
        self.bluetooth_server.iniciar()
        
    def run(self):
        self.t = Thread(target = self.start_bluetooth)
        self.t.daemon = True
        self.t.start()
        # Bucle de GTK+3
        Gtk.main()

    def destroy(self, widget):
        self.bluetooth_server.desconectar()
        # Cerrar GTK+3
        Gtk.main_quit()

    def add_page(self, title, frame):
        l = Gtk.Label(title)
        l.show()
        self.notebook.append_page(frame, l)

    def show_all(self):
        self.debug("show_all")
        self.vbox.show()
        self.notebook.show()
        self.bar.show()
        self.show()

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
            self.last_array = data.split(',')
        elif self.mode == MODO_BEATBOX:
            self.last_beat = float(data) # float('{0:.2f}'.format(data))
        else:
            self.debug("Modo incorrecto")

    def on_status_changed(self, msg):
        if hasattr(self, 'bar'):
            self.bar.push(0, msg)

    def on_beatbox_draw(self, w, cr):
        cr.set_source_rgba(1, 0.75, 0.25, self.last_beat)
        cr.arc(320, 240, 200, 0, 2 * math.pi)
        cr.fill_preserve()

        cr.set_source_rgb(0, 0, 0)
        cr.stroke()

        cr.arc(240, 180, 40, 0, 2 * math.pi)
        cr.arc(400, 180, 40, 0, 2 * math.pi)
        cr.fill()

        cr.set_line_width(20)
        cr.set_line_cap(cairo.LINE_CAP_ROUND)
        cr.arc(320, 240, 120, 
            math.pi / 4 + 2 * self.last_beat * math.pi, 
            math.pi * 3 / 4 + 2 * self.last_beat * math.pi)
        cr.stroke()

    def on_spectrum_draw(self, w, cr):
        cr.set_line_width(20)
        cr.set_line_cap(cairo.LINE_CAP_SQUARE)

        for i, v in enumerate(self.last_array):
            cr.set_source_rgba(v, 1 - v, 0, 1)
            cr.move_to(16 + i * 40, 470)
            cr.line_to(16 + i * 40, 470 - 460 * v)
        cr.stroke()

    def debug(self, msg):
        if DEBUG:
            print "[application] %s" % msg

if __name__ == "__main__":
    app = LEDPartyServer()
    try:
        app.run()
    except KeyboardInterrupt:
        app.destroy(app)

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Servidor;

import Utilidades.ConstructorMensajes;
import Utilidades.Coordenadas;
import Utilidades.Direccion;
import static java.lang.Double.max;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author millenium
 */
public class ModeloJuego extends Observable {

    private int columnas, filas;
    private Map<Integer, Jugador> jugadores;
    private final int VELOCIDAD = 750;
    private final int TAMAÑOBASE = 3;
    private final int PUNTOSTESORO = 100;
    private ThreadActualizarTablero hiloTablero;
    private List<Coordenadas> tesoros;

    public ModeloJuego() {
        this.jugadores = new ConcurrentHashMap<>();
        this.tesoros = Collections.synchronizedList(new ArrayList<>());
    }

    public void añadirJugador() {
        boolean empezarJuego = this.jugadores.isEmpty();
        if (!empezarJuego) {
            this.hiloTablero.pausa();
        }
        int key = siguienteKey();
        Jugador nuevo = new Jugador(this.TAMAÑOBASE);
        asignarCoordInicio(nuevo, key);
        synchronized (this.jugadores) {
            this.jugadores.put(key, nuevo);
        }
        if (empezarJuego) {
            this.hiloTablero = new ThreadActualizarTablero(this);
            this.hiloTablero.start();
            this.generarTesoro();
        }
        setChanged();
        notifyObservers("NJ;" + key);
        if (!empezarJuego) {
            this.hiloTablero.pausa();
        }
    }

    public int siguienteKey() {
        //posible problema de las keys
        int key = 1;
        synchronized (this.jugadores) {
            while (this.jugadores.keySet().contains(key)) {
                key++;
            }
        }
        return key;
    }

    public int getFilas() {
        return this.filas;
    }

    public int getColumnas() {
        return this.columnas;
    }

    public int getTamañoBase() {
        return this.TAMAÑOBASE;
    }

    public int getVELOCIDAD() {
        return VELOCIDAD;
    }

    public Map<Integer, Jugador> getJugadores() {
        return jugadores;
    }

    public Coordenadas getCabeza(int id) {
        synchronized (this.jugadores) {
            return this.jugadores.get(id).getCabeza();
        }
    }

    public Coordenadas getAnteriorCola(int id) {
        synchronized (this.jugadores) {
            return this.jugadores.get(id).getAnteriorCola();
        }
    }

    public int getPUNTOSTESORO() {
        return PUNTOSTESORO;
    }

    public List<Coordenadas> getTesoros() {
        return tesoros;
    }

    private void asignarCoordInicio(Jugador jugador, int id) {
        Random r = new Random();
        int nuevoX, nuevoY;
        boolean fin = false;
        int margenColumnas = (int) max(this.TAMAÑOBASE, this.columnas * 0.2);
        int margenFilas = (int) max(this.TAMAÑOBASE, this.filas * 0.2);
        while (!fin) {
            //Se asigna una coordenada aleatoria con un margen con los bordes
            nuevoX = r.nextInt(this.columnas - (2 * margenColumnas)) + margenColumnas;
            nuevoY = r.nextInt(this.filas - (2 * margenFilas)) + margenFilas;
            //System.out.println("X: " + nuevoX + " Y: " + nuevoY); //Pruebas coordenadas iniciales
            for (int i = 0; i < this.TAMAÑOBASE; i++) {
                Coordenadas coord = new Coordenadas(nuevoX + i, nuevoY);
                if (colisionJugador(coord, id) != 0) { //Se intenta de nuevo
                    jugador.eliminarSerpiente();
                    break;
                } else {
                    jugador.nuevaCabeza(coord);
                }
                if ((i == this.TAMAÑOBASE - 1)) {
                    fin = true;
                }
            }
        }
    }

    private int colisionJugador(Coordenadas coord, int id) { //Solo comprueba con el resto de serpientes (devuelve id del choque, o 0 si no choca)
        synchronized (this.jugadores) {
            for (Map.Entry<Integer, Jugador> entrada : this.jugadores.entrySet()) {
                LinkedList<Coordenadas> serpiente = entrada.getValue().getSerpiente();
                int primero;
                if (id == entrada.getKey()) { //Si se revisa la colisión con uno mismo, salta la cabeza (para evitar errores)
                    primero = 1;
                } else {
                    primero = 0;
                }
                for (int i = primero; i < serpiente.size(); i++) {
                    Coordenadas comparar = serpiente.get(i);
                    if (coord.equals(comparar)) {
                        return entrada.getKey();
                    }
                }
            }
        }
        return 0;
    }

    private boolean colisionBorde(Coordenadas coord) {
        int x = coord.getX();
        int y = coord.getY();
        return ((x < 0) || (y < 0) || (x >= this.columnas) || (y >= this.filas));
    }

    public void cambiarDireccion(Direccion direccion, int id) {
        synchronized (this.jugadores) {
            Jugador jugador = this.jugadores.get(id);
            if (!jugador.isEspera()) { //Comprueba que se pueda cambiar dirección
                /*if (((direccion == Direccion.ARRIBA) && (direccion != Direccion.ABAJO)) //No se puede mover en la dirección contraria
                        || ((direccion == Direccion.ABAJO) && (direccion != Direccion.ARRIBA))
                        || ((direccion == Direccion.IZQ) && (direccion != Direccion.DER))
                        || ((direccion == Direccion.DER) && (direccion != Direccion.IZQ))) {
                    jugador.setDireccion(direccion);
                    jugador.setEspera(true); //No se puede volver a cambiar dirección hasta siguiente ciclo
                }*/
                if (jugador.getDireccion().equals(Direccion.IZQ) && !(direccion.equals(Direccion.DER) || direccion.equals(Direccion.IZQ))) {
                    jugador.setDireccion(direccion);
                    jugador.setEspera(true); //No se puede volver a cambiar dirección hasta siguiente ciclo
                } else if (jugador.getDireccion().equals(Direccion.DER) && !(direccion.equals(Direccion.DER) || direccion.equals(Direccion.IZQ))) {
                    jugador.setDireccion(direccion);
                    jugador.setEspera(true); //No se puede volver a cambiar dirección hasta siguiente ciclo
                } else if (jugador.getDireccion().equals(Direccion.ARRIBA) && !(direccion.equals(Direccion.ARRIBA) || direccion.equals(Direccion.ABAJO))) {
                    jugador.setDireccion(direccion);
                    jugador.setEspera(true); //No se puede volver a cambiar dirección hasta siguiente ciclo
                } else if (jugador.getDireccion().equals(Direccion.ABAJO) && !(direccion.equals(Direccion.ARRIBA) || direccion.equals(Direccion.ABAJO))) {
                    jugador.setDireccion(direccion);
                    jugador.setEspera(true); //No se puede volver a cambiar dirección hasta siguiente ciclo
                }
            }
        }
    }

    public void eliminarJugador(int id) {
        synchronized (this.jugadores) {
            this.jugadores.remove(id);
        }
    }

    public void finalizarJugador(int id) {
        this.eliminarJugador(id);
        notifyObservers("FIN;" + id);
    }

    public boolean hayJugadores() {
        return !this.jugadores.isEmpty();
    }

    public Coordenadas[] getCoordenadasAnt(int id) {
        Jugador jugador = this.jugadores.get(id);
        Coordenadas[] coordenadasAnt = new Coordenadas[jugador.getSerpiente().size()];
        LinkedList<Coordenadas> serpiente = jugador.getSerpiente();
        for (int i = 1; i < serpiente.size(); i++) {
            coordenadasAnt[i - 1] = serpiente.get(i);
        }
        coordenadasAnt[serpiente.size() - 1] = jugador.getAnteriorCola();
        return coordenadasAnt;
    }

    public Coordenadas[] getCoordenadas(int id) {
        Jugador jugador = this.jugadores.get(id);
        Coordenadas[] coordenadas = new Coordenadas[jugador.getSerpiente().size()];
        jugador.getSerpiente().toArray(coordenadas);
        return coordenadas;
    }

    public void notificarMovimiento(int id) { //Revisa las colisiones, si no colisiona envia movimiento y comprueba tesoro
        synchronized (this.jugadores) {
            int idColision;
            setChanged();

            if (!this.jugadores.isEmpty()) {
                if ((idColision = this.colisionJugador(this.jugadores.get(id).getCabeza(), id)) != 0) {
                    notifyObservers("COL;" + id + ";" + idColision);
                    this.eliminarJugador(id);
                    if (idColision != id) { //Si la colisión es entre 2 jugadores elimina al segundo
                        this.eliminarJugador(idColision);
                    }
                }
            }
            if (!this.jugadores.isEmpty()) {
                if (this.colisionBorde(this.jugadores.get(id).getCabeza())) {
                    notifyObservers("CBR;" + id);
                    this.eliminarJugador(id);
                } else {
                    notifyObservers("MOV;" + id);
                    if (!this.jugadores.isEmpty()) {
                        if (this.colisionTesoro(this.jugadores.get(id).getCabeza())) {
                            this.jugadores.get(id).añadirPuntos(this.PUNTOSTESORO);
                            int nuevosPuntos = this.jugadores.get(id).getPuntos();
                            setChanged();
                            notifyObservers("PTS;" + id + ";" + nuevosPuntos);
                        }
                    }
                }
            }
        }
    }

    public void generarTesoro() {
        Random r = new Random();
        Coordenadas candidato = new Coordenadas(r.nextInt(this.columnas), r.nextInt(this.filas));
        while (colisionJugador(candidato, 0) != 0) {
            candidato = new Coordenadas(r.nextInt(this.columnas), r.nextInt(this.filas));
        }
        this.tesoros.add(candidato);
        setChanged();
        notifyObservers("TSR;" + candidato.getX() + ";" + candidato.getY());
    }

    private boolean colisionTesoro(Coordenadas coord) {
        Iterator<Coordenadas> iter = this.tesoros.iterator();
        while (iter.hasNext()) {
            Coordenadas tesoro = iter.next();
            if (coord.equals(tesoro)) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    public void setFilasColumnas(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
    }
}

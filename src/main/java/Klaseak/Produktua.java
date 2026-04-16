package Klaseak;

public class Produktua {
    private int id;
    private String izena;
    private double prezioa;
    private int motaId;
    private int stock;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public double getPrezioa() { return prezioa; }
    public void setPrezioa(double prezioa) { this.prezioa = prezioa; }

    public int getMotaId() { return motaId; }
    public void setMotaId(int motaId) { this.motaId = motaId; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

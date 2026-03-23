package Klaseak;

public class Osagaia {
    private int id;
    private String izena;
    private double prezioa;
    private int stock;
    private int hornitzaileakId;
    private int gutxienekoStock = 10;
    private boolean eskatu = false;

    public Osagaia() {
    }

    public Osagaia(int id, String izena, double prezioa, int stock, int hornitzaileakId) {
        this.id = id;
        this.izena = izena;
        this.prezioa = prezioa;
        this.stock = stock;
        this.hornitzaileakId = hornitzaileakId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }

    public double getPrezioa() { return prezioa; }
    public void setPrezioa(double prezioa) { this.prezioa = prezioa; }

    public double getAzkenPrezioa() { return prezioa; }
    public void setAzkenPrezioa(double azkenPrezioa) { this.prezioa = azkenPrezioa; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getGutxienekoStock() { return gutxienekoStock; }
    public void setGutxienekoStock(int gutxienekoStock) { this.gutxienekoStock = gutxienekoStock; }

    public boolean isEskatu() { return eskatu; }
    public void setEskatu(boolean eskatu) { this.eskatu = eskatu; }

    public int getHornitzaileakId() { return hornitzaileakId; }
    public void setHornitzaileakId(int hornitzaileakId) { this.hornitzaileakId = hornitzaileakId; }

    public boolean erosiBeharDa() {
        return stock <= gutxienekoStock;
    }

    public double stockBalioaLortu() {
        return stock * getAzkenPrezioa();
    }

    @Override
    public String toString() {
        return izena + " (Stock: " + stock + ", Prezioa: " + getAzkenPrezioa() + "€)";
    }
}

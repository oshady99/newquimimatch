package com.quimimatch.screens;
import com.badlogic.gdx.graphics.Color;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.board.AtomType;
import com.quimimatch.managers.*;

public class ShopScreen extends ReferenceScreen {
    private int tab;
    private String feedback="Compra con monedas del juego. Sin pagos reales.";
    private static final String[] IDS={"default","neon","pastel","metallic","galaxy"};
    private static final String[] NAMES={"Clasico","Neon","Pastel","Metalico","Galaxia"};
    private static final int[] PRICES={0,80,60,100,150};
    private static final String[] AIDS={"+5 pulsos","Purga","Comodin"};
    private static final String[] DESC={"Agrega cinco disparos disponibles.","Elimina cargas rojas y repara 15%.","Envia un atomo necesario sin gastar pulso."};
    private static final int[] COST={30,50,75};
    public ShopScreen(QuimiMatchGame game){super(game);}
    @Override protected void draw(float dt) {
        PlayerInventory inv=PlayerInventory.get();
        text("TIENDA DEL LABORATORIO",35,680,2.4f,Color.GOLD);
        text("Monedas: "+inv.getCoins(),930,678,1.9f,Color.GOLD);
        button("AYUDAS",35,575,580,tab==0);button("APARIENCIAS",635,575,610,tab==1);
        if(tab==0) {
            for(int i=0;i<3;i++) {
                float y=405-i*137;
                box(35,y,1210,118,new Color(.07f,.14f,.24f,1));
                text(AIDS[i],60,y+87,1.9f,Color.CYAN);
                text(DESC[i],60,y+52,1.45f,Color.WHITE);
                int stock=i==0?inv.getMoves():i==1?inv.getBombs():inv.getWildcards();
                text("Tienes: "+stock,700,y+53,1.4f,Color.LIGHT_GRAY);
                button("COMPRAR · "+COST[i],920,y+28,295,inv.getCoins()>=COST[i]);
            }
        } else {
            for(int i=0;i<5;i++) {
                float x=35+(i%3)*410,y=330-(i/3)*205;
                box(x,y,390,185,new Color(.07f,.14f,.24f,1));
                text(NAMES[i],x+20,y+151,1.8f,Color.CYAN);
                sr.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
                int n=0;
                for(AtomType atom:new AtomType[]{AtomType.H,AtomType.O,AtomType.C,AtomType.N}) {
                    sr.setColor(SkinPalette.getColor(IDS[i],atom)); sr.circle(x+65+n++*80,y+97,23,24);
                }
                sr.end();
                String action=inv.getActiveSkin().equals(IDS[i])?"ACTIVA":inv.isSkinUnlocked(IDS[i])?"ACTIVAR":"COMPRAR · "+PRICES[i];
                button(action,x+20,y+12,350,inv.getActiveSkin().equals(IDS[i]));
            }
        }
        text(feedback,280,88,1.45f,Color.GOLD);
        button("< MENU",35,25,210,false);button("ENCICLOPEDIA",945,25,300,false);
        text("Las ayudas se usan desde el laboratorio. Las apariencias cambian sus atomos.",280,37,1.1f,Color.LIGHT_GRAY);
    }
    @Override protected void tap(float x,float y) {
        if(inside(x,y,35,25,210,58)){game.setScreen(new MenuScreen(game));return;}
        if(inside(x,y,945,25,300,58)){game.setScreen(new EncyclopediaScreen(game));return;}
        if(inside(x,y,35,575,580,58)){tab=0;return;}
        if(inside(x,y,635,575,610,58)){tab=1;return;}
        PlayerInventory inv=PlayerInventory.get();
        if(tab==0) {
            for(int i=0;i<3;i++)if(inside(x,y,920,433-i*137,295,58)) {
                boolean ok=i==0?inv.buyMoves(COST[i]):i==1?inv.buyBomb(COST[i]):inv.buyWildcard(COST[i]);
                feedback=ok?"Comprado: "+AIDS[i]+". Disponible en el laboratorio.":"Monedas insuficientes. Gana monedas jugando.";
                if(ok){SaveManager.saveInventory();AudioManager.get().playMenuClick();}return;
            }
        } else for(int i=0;i<5;i++)if(inside(x,y,55+(i%3)*410,342-(i/3)*205,350,58)) {
            boolean ok;
            if(inv.isSkinUnlocked(IDS[i])){inv.activateSkin(IDS[i]);ok=true;}else ok=inv.buySkin(IDS[i],PRICES[i]);
            feedback=ok?"Apariencia activa: "+NAMES[i]:"Monedas insuficientes.";
            if(ok){SaveManager.saveInventory();AudioManager.get().playMenuClick();}return;
        }
    }
}

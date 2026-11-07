package com.quimimatch.screens;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.quimimatch.QuimiMatchGame;
import com.quimimatch.managers.MoleculeDatabase;
import com.quimimatch.managers.MoleculeDatabase.MoleculeEntry;
import java.util.*;

public class EncyclopediaScreen extends ReferenceScreen {
    private int tab=0,page=0;
    private final List<MoleculeEntry> entries=new ArrayList<>();
    private static final String[][] TIPS={
        {"Atomo y molecula", "Un atomo es una unidad de un elemento. Una molecula contiene atomos unidos: H2O tiene dos de hidrogeno y uno de oxigeno.\n\nTIP DE BYTE\nCambiar un subindice cambia la composicion: H2O y H2O2 son sustancias diferentes. Nunca intercambies productos por tener nombres parecidos."},
        {"Mezcla o sustancia", "En una mezcla se juntan sustancias sin formar necesariamente otra nueva. El agua con sal es una mezcla; la sal sigue presente aunque no veas los cristales.\n\nTIP DE BYTE\nNo todo esta hecho de moleculas aisladas. La sal de mesa forma una red de iones y los metales tienen otra estructura."},
        {"Disolver no es desaparecer", "Al disolverse, las particulas se distribuyen por el agua. Algunas sustancias se disuelven con facilidad y otras no.\n\nTIP DE BYTE\nEl agua disuelve muchas sustancias, pero no todas. El aceite, por ejemplo, no se mezcla uniformemente con ella."},
        {"Cambios fisicos y quimicos", "Derretir hielo y evaporar agua son cambios fisicos: el agua sigue siendo H2O. En una reaccion quimica se forman sustancias diferentes.\n\nTIP DE BYTE\nUna burbuja por si sola no demuestra una reaccion: al hervir agua tambien hay burbujas. Observa y compara varias evidencias."},
        {"Acidos y bases", "El pH ayuda a describir si una disolucion acuosa es acida o basica. La concentracion tambien influye en su comportamiento.\n\nTIP DE BYTE\nNo pruebes ni huelas directamente una sustancia para identificarla. Nunca mezcles productos de limpieza."},
        {"Conservacion de los atomos", "En una reaccion quimica ordinaria los atomos se reorganizan. Una ecuacion balanceada tiene la misma cantidad de cada elemento en ambos lados.\n\nTIP DE BYTE\nBalancea cambiando los coeficientes de la ecuacion; no cambies los subindices de las formulas."},
        {"Estrategia del laboratorio", "Mira los contadores antes de disparar. Cada pulso incorpora hasta tres atomos del tipo elegido, sin exceder el objetivo.\n\nTIP DE BYTE\nActiva el escudo cuando la carga roja se acerque: dura poco y debe recargarse. Los atomos que ya completaste reducen la estabilidad."},
        {"Ciencia y juego", "Los colores, disparos y enemigos son recursos del juego. Un objetivo de recoleccion no representa necesariamente la proporcion exacta de una molecula. Las aleaciones son mezclas y las proteinas son familias de sustancias.\n\nTIP DE BYTE\nAntes de experimentar: pregunta, predice, observa, anota y explica. Repite cambiando una sola variable."}
    };
    private static final String[][] EXP={
        {"1. ¿A donde fue la sal? · 5 minutos", "MATERIALES\nDos vasos plasticos transparentes, agua a temperatura ambiente, sal de mesa y cuchara.\n\nPASOS\n1. Pon 50 mL de agua en cada vaso.\n2. Agrega 1/4 de cucharadita de sal a uno y remueve 30 segundos. Deja el otro como control.\n3. Compara lo que ves. Anota tu prediccion antes de remover.\n\n¿QUE OCURRE?\nLos cristales dejan de verse al disolverse; la sal permanece distribuida en el agua. No es una sustancia nueva.\n\nCUIDADO: con un adulto. No beber ni probar. Lava manos y utensilios."},
        {"2. Agua y aceite · 5 minutos", "MATERIALES\nVaso plastico transparente, 50 mL de agua, 1 cucharadita de aceite vegetal y cuchara.\n\nPASOS\n1. Agrega el aceite al agua y observa las capas.\n2. Remueve suavemente durante 15 segundos.\n3. Espera dos minutos y compara con lo que ocurrio al remover.\n\n¿QUE OCURRE?\nEl aceite se dispersa temporalmente en gotas y vuelve a separarse. No se mezcla uniformemente con el agua; en este caso queda arriba por su menor densidad.\n\nCUIDADO: con un adulto. No ingerir. Evita derrames. Recoge el aceite con papel y desecha en la basura; no lo viertas al desague."},
        {"3. Recupera los cristales · varios dias", "MATERIALES\nPlato plastico poco profundo, agua, sal y cuchara.\n\nPASOS\n1. Disuelve 1/2 cucharadita de sal en 2 cucharaditas de agua a temperatura ambiente.\n2. Vierte la disolucion en el plato.\n3. Dejalo en un sitio protegido de mascotas y ninos pequenos. Observa cada dia hasta que se seque.\n\n¿QUE OCURRE?\nEl agua se evapora y la sal queda en forma solida. El tiempo depende del ambiente: puede tardar varios dias.\n\nCUIDADO: con un adulto. No calentar ni usar fuego. No probar los cristales. Lava manos y utensilios."}
    };
    public EncyclopediaScreen(QuimiMatchGame game) {
        super(game); for(MoleculeEntry e:MoleculeDatabase.getAll())if(e.discovered)entries.add(e);
    }
    private int count(){return tab==0?Math.max(1,entries.size()):tab==1?TIPS.length:EXP.length;}
    @Override protected void draw(float dt) {
        text("ENCICLOPEDIA QUIMICA",35,678,2.4f,Color.GOLD);
        text("Aprende con Byte · disponible sin conexion",35,638,1.4f,Color.LIGHT_GRAY);
        String[] tabs={"MOLECULAS","TIPS DE BYTE","EXPERIMENTOS"};
        for(int i=0;i<3;i++)button(tabs[i],35+i*410,550,390,tab==i);
        box(35,125,1210,402,new Color(.06f,.12f,.22f,1));
        String title,content;
        if(tab==0) {
            if(entries.isEmpty()){title="Tu coleccion esta comenzando";content="Completa un nivel para descubrir sus datos.\n\nMientras tanto, puedes leer todos los tips de Byte y los experimentos: no necesitan desbloqueo.";}
            else {MoleculeEntry e=entries.get(page);title=e.name+" · "+(e.formula.equals("C2H2O")?"C2H4O":e.formula.equals("HCl+Na")?"HCl":e.formula);content=e.fact+"\n\nPROPIEDADES\n"+e.property1+". "+e.property2+"\n\nUSOS\n"+e.uses+"\n\nLas fichas son informativas: no son instrucciones para manipular estas sustancias.";}
        } else {String[] e=(tab==1?TIPS:EXP)[page]; title=e[0];content=e[1];}
        text(title,60,505,1.85f,Color.CYAN); body(content,60,465,1155);
        button("< MENU",35,35,200,false);button("< ANTERIOR",310,35,230,page>0);
        text((page+1)+" / "+count(),590,73,1.6f,Color.WHITE);
        button("SIGUIENTE >",710,35,230,page<count()-1);
        if(tab==2)button("FUENTE ACS",970,35,275,false);
    }
    @Override protected void tap(float x,float y) {
        if(inside(x,y,35,35,200,58)){game.setScreen(new MenuScreen(game));return;}
        for(int i=0;i<3;i++)if(inside(x,y,35+i*410,550,390,58)){tab=i;page=0;return;}
        if(inside(x,y,310,35,230,58))page=Math.max(0,page-1);
        if(inside(x,y,710,35,230,58))page=Math.min(count()-1,page+1);
        if(tab==2&&inside(x,y,970,35,275,58))Gdx.net.openURI("https://www.acs.org/education/resources/k-8/inquiryinaction/fifth-grade.html");
    }
}

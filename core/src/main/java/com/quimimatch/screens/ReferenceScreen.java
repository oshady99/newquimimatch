package com.quimimatch.screens;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.quimimatch.QuimiMatchGame;

/** Shared landscape layout for the shop and reading pages. */
abstract class ReferenceScreen extends ScreenAdapter {
    protected final QuimiMatchGame game;
    protected final ShapeRenderer sr = new ShapeRenderer();
    protected final BitmapFont font = new BitmapFont();
    protected final GlyphLayout layout = new GlyphLayout();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(1280,720,camera);
    private final Vector2 point = new Vector2();
    private boolean disposed;
    ReferenceScreen(QuimiMatchGame game) { this.game=game; resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()); }
    @Override public final void render(float dt) {
        Gdx.gl.glClearColor(.025f,.045f,.11f,1); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply(); game.batch.setProjectionMatrix(camera.combined); sr.setProjectionMatrix(camera.combined);
        draw(dt);
        if(Gdx.input.justTouched()) {
            point.set(Gdx.input.getX(),Gdx.input.getY()); viewport.unproject(point); tap(point.x,point.y);
        }
    }
    protected abstract void draw(float dt);
    protected abstract void tap(float x,float y);
    protected void box(float x,float y,float w,float h,Color c) {
        sr.begin(ShapeRenderer.ShapeType.Filled); sr.setColor(c); sr.rect(x,y,w,h); sr.end();
    }
    protected void text(String value,float x,float y,float scale,Color c) {
        font.getData().setScale(scale); font.setColor(c); game.batch.begin(); font.draw(game.batch,value,x,y); game.batch.end();
    }
    protected void body(String value,float x,float y,float width) {
        font.getData().setScale(1.65f); font.setColor(Color.WHITE);
        layout.setText(font,value,Color.WHITE,width,Align.left,true);
        for(int i=0;i<12 && layout.height>325;i++) {
            font.getData().setScale(font.getData().scaleX * 0.94f);
            layout.setText(font,value,Color.WHITE,width,Align.left,true);
        }
        game.batch.begin(); font.draw(game.batch,layout,x,y); game.batch.end();
    }
    protected void button(String label,float x,float y,float w,boolean active) {
        box(x,y,w,58,active?new Color(.08f,.48f,.58f,1):new Color(.13f,.21f,.33f,1));
        text(label,x+18,y+38,1.55f,Color.WHITE);
    }
    protected boolean inside(float x,float y,float bx,float by,float w,float h) {return x>=bx&&x<=bx+w&&y>=by&&y<=by+h;}
    @Override public void resize(int w,int h) {viewport.update(w,h,true);}
    @Override public void hide() {
        Gdx.gl.glViewport(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        game.batch.getProjectionMatrix().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()); dispose();
    }
    @Override public void dispose() { if(!disposed){disposed=true; sr.dispose();font.dispose();} }
}

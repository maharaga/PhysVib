package example.demonstration;
import PhysVib.VibrationConverter.CustomAudioOutput;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.background.*;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.AutoWrap;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.util.HorizontalAlign;

import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import PhysVib.CollisionCatcher;
import PhysVib.Data.userdata;



public class ExampleDemonstration extends CustomSimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	public static final int CAMERA_WIDTH = 640;
	public static final int CAMERA_HEIGHT = 360;
	public static final float PIXEL_TO_METER_RATIO = 1600f;
	public static final float ptm = 40f;
	
	
	public static final int GROUND = 0;
	public static final int ROOF = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;
	public static final int RECTANGLE = 4;
	public static final int CIRCLE = 5;
	public static final int WALLS = 6;
	
	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mTexture;


	private PhysicsWorld mPhysicsWorld;

	private final Vector2 mTempVector = new Vector2();
	private final float[] mTempVector3 = new float[3];
	
	private CollisionCatcher mContactListener = new CollisionCatcher();
	
	private float anchorX = 0f;
	private float anchorY = 0f;
	private float springX = 0f;
	private float springY = 0f;
	private Body BirdsBody = null;
	
	//private AnimatedSprite keepSprite = null;
	private TiledSprite keepSprite = null;
	private int touchAction = -1;
	private float touchX = -1;
	private float touchY = -1;
	long lastTime = 0;
	
	public static final int MENU_SCENE1 = 101;
	public static final int MENU_SCENE2 = 102;
	public static final int MENU_EXPERIMENT = 103;
	public static final int MENU_SELECT = 104;
	
	
	public static final int EXPERIMENT_ALLRENDER = 1001;	
	public static final int EXPERIMENT_AMPRENDER = 1002;
	public static final int EXPERIMENT_TIMERENDER = 1003;
	public static final int EXPERIMENT_NORENDER = 1004;
	public static final int EXPERIMENT_NOVIB = 1005;
	
	public static final int EXPERIMENT_SCENE_1 = 10001;
	public static final int EXPERIMENT_SCENE_2 = 10002;
	
	public int currentMenu = MENU_EXPERIMENT;
	public int currentScene = EXPERIMENT_SCENE_1;
	
	private Scene mScene;
	
	static public Handler mMainHandler = null;
	
	private CustomAudioOutput myOutput = new CustomAudioOutput();
	//private CustomAndroidVibrationAPI myAV = null;
	
	private TiledSprite firstCondition = null;
	private Body firstConditionBody = null;
	private TiledSprite secondCondition = null;
	private Body secondConditionBody = null;
	private TiledSprite thirdCondition = null;
	private Body thirdConditionBody = null;
	
	private TiledSprite firstWallCondition = null;
	private Body firstWallConditionBody = null;
	private TiledSprite secondWallCondition = null;
	private Body secondWallConditionBody = null;
	private TiledSprite thirdWallCondition = null;
	private Body thirdWallConditionBody = null;
	
	private TiledTextureRegion wallObjectTextureRegion;

	private TiledTextureRegion firstConditionTextureRegion;
	private TiledTextureRegion secondConditionTextureRegion;
	private TiledTextureRegion thirdConditionTextureRegion;
	
	private TextureRegion mWoodTextureRegion;
	private TextureRegion mConcreteTextureRegion;
	private TextureRegion mSteelTextureRegion;

	private TiledTextureRegion mWoodenBallTextureRegion;
	private TiledTextureRegion mMetalCanTextureRegion;
	private TiledTextureRegion mBeachBallTextureRegion;	
	
	private BitmapTextureAtlas mFontTexture;
	private org.andengine.opengl.font.Font mFont;
	private Entity fruitLayer;
	private Entity scoreLayer;
	
	private TiledTextureRegion objectRegion = null;
	
	private Text timerNumber = null;
	

	public int curDirection = 0;
	public float curDirectionTick = 0;
	public int prevDirection = -1;
	public boolean directionFlag = false;
	public boolean restFlag = false;

	// 2016.06.01 Added Variables
	List<ObjectManager> rubberList = new ArrayList<ObjectManager>();
	List<ObjectManager> woodList = new ArrayList<ObjectManager>();
	List<ObjectManager> steelList = new ArrayList<ObjectManager>();
	List<ObjectManager> concreteGroundList = new ArrayList<ObjectManager>();
	List<ObjectManager> woodenGroundList = new ArrayList<ObjectManager>();
	List<ObjectManager> steelGroundList = new ArrayList<ObjectManager>();

	float elapsedTime = 0;
	boolean resetFlag = false;
	float objectScale = 0.5f;
	
	public void displayText(Text inText, int sequence, int mode) {	
		CharSequence inSequence = null;
		if (mode == 0) 
			inSequence = " ";
		else if (mode == 1)
			inSequence = " pause";
		else if (mode == 2)
			inSequence = " take a rest";
			
					
		if (sequence == 0) 
			inText.setText("AFD" + inSequence);
		else if (sequence == 1) 
			inText.setText("AF" + inSequence);
		else if (sequence == 2) 
			inText.setText("AD" + inSequence);
		else if (sequence == 3) 
			inText.setText("FD" + inSequence);
		else if (sequence == 4) 
			inText.setText("A" + inSequence);
		else if (sequence == 5) 
			inText.setText("F" + inSequence);
		else if (sequence == 6) 
			inText.setText("D" + inSequence);
		else if (sequence == 7) 
			inText.setText("Impulse" + inSequence);
				
		inText.setX(CAMERA_WIDTH / 2 - 300 - inText.getWidthScaled());		
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH , CAMERA_HEIGHT );
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);

		return engineOptions;
	}
	
	@Override
	public void onCreateResources() {		
		FontFactory.setAssetBasePath("font/");
		mFontTexture = new BitmapTextureAtlas(getTextureManager(), 512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = FontFactory.createFromAsset(getFontManager(), mFontTexture,
				getAssets(), "arial.ttf", 64, true, Color.WHITE);

		getTextureManager().loadTexture(this.mFontTexture);
		getFontManager().loadFont(this.mFont);

		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mTexture = new BitmapTextureAtlas(this.getTextureManager(), 4096, 4096);
		this.mWoodTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mTexture, this.getAssets(), "WoodTexture.png", 0, 0);
		this.mConcreteTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mTexture, this.getAssets(),
						"CementTexture.png", 600, 0);
		this.mSteelTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mTexture, this.getAssets(),
						"Metal 1024 768.png", 1300, 0);		
		this.mWoodenBallTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"WoodenBall.png", 0, 1024, 1, 1);
		this.mBeachBallTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"BeachBall.png", 600, 1024, 1, 1);
		this.mMetalCanTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"MetalCan.png", 1024, 1024, 1, 1);

		this.firstConditionTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"focus.png", 0, 2048, 2, 1);
		this.secondConditionTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"focus.png", 700, 2048, 2, 1);
		this.thirdConditionTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"focus.png", 1400, 2048, 2, 1);

		this.wallObjectTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(this.mTexture, this.getAssets(),
						"wallobject.png", 2100, 2048, 2, 1);
		this.mTexture.load();
		this.enableAccelerationSensor(this);			
	}
	

	
	
	void createWoodenBall(float px, float py) {			
		userdata tempUserData = new userdata(false);
		ObjectManager tempManager = new ObjectManager();
			    	
    	FixtureDef objectFixtureDef;
    	    
		objectRegion = this.mWoodenBallTextureRegion.deepCopy();
		
		objectFixtureDef = PhysicsFactory.createFixtureDef(850f, 0.603f, 0.5f);
				
		tempManager.objectFace = new AnimatedSprite(0, 0, objectRegion, this.getVertexBufferObjectManager());
		tempManager.objectFace.setScaleCenter(tempManager.objectFace.getWidth() / 2, tempManager.objectFace.getHeight() / 2);
		float radius = (float)Math.random() + 0.5f;
		if (radius > 1f) 
			radius = 1f;
		
		radius *= objectScale;
		tempManager.objectFace.setScale(0.2f * radius);
		
		
		tempManager.objectBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.DynamicBody, objectFixtureDef, ptm);

		tempManager.objectBody.setTransform(px / ptm, py / ptm, (float)(Math.random() * Math.PI));

		tempUserData = new userdata(false);	
		tempUserData.objectID = 4;
		tempUserData.naturalFrequency = 349f;
		tempUserData.decayRate = 106.9971f;

		tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
		tempManager.objectBody.setBullet(true);

		fruitLayer.attachChild(tempManager.objectFace);
		tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
		this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);

		mScene.registerTouchArea(tempManager.objectFace);
		
		woodList.add(tempManager);
	}
	
	void createRubberBall(float px, float py) {
		userdata tempUserData = new userdata(false);
		ObjectManager tempManager = new ObjectManager();
			
    	FixtureDef objectFixtureDef;
    	    
		objectRegion = this.mBeachBallTextureRegion.deepCopy();
		
		objectFixtureDef = PhysicsFactory.createFixtureDef(120f, 0.85f,	0.688f);
				
		tempManager.objectFace = new AnimatedSprite(0, 0, objectRegion, this.getVertexBufferObjectManager());
		tempManager.objectFace.setScaleCenter(tempManager.objectFace.getWidth() / 2, tempManager.objectFace.getHeight() / 2);
		float radius = (float)Math.random() + 0.5f;
		if (radius > 1f) 
			radius = 1f;
		
		radius *= objectScale;
		
		tempManager.objectFace.setScale(0.285f * radius);
		
		
		tempManager.objectBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.DynamicBody, objectFixtureDef, ptm);

		tempManager.objectBody.setTransform(px / ptm, py / ptm, (float)(Math.random() * Math.PI));

		tempUserData = new userdata(false);
		tempUserData.objectID = 5;
		tempUserData.naturalFrequency = 80f;
		tempUserData.decayRate = 34.1268f;

		tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
		tempManager.objectBody.setBullet(true);

		fruitLayer.attachChild(tempManager.objectFace);
		tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
		this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);

		mScene.registerTouchArea(tempManager.objectFace);
		
		rubberList.add(tempManager);
	}
	
	
	void createSteelCan (float px, float py) {
		userdata tempUserData = new userdata(false);
		ObjectManager tempManager = new ObjectManager();
			

    	FixtureDef objectFixtureDef;
    	    
		objectRegion = this.mMetalCanTextureRegion.deepCopy();		
		objectFixtureDef = PhysicsFactory.createFixtureDef(1193f, 0.597f, 0.78f);
		
				
		tempManager.objectFace = new AnimatedSprite(0, 0, objectRegion, this.getVertexBufferObjectManager());
		tempManager.objectFace.setScaleCenter(tempManager.objectFace.getWidth() / 2, tempManager.objectFace.getHeight() / 2);
		float radius = (float)Math.random() + 0.5f;
		if (radius > 1f) 
			radius = 1f;
		
		radius *= objectScale;
		tempManager.objectFace.setScale(0.214f * radius);
		
		
		tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.DynamicBody, objectFixtureDef, ptm);

		tempManager.objectBody.setTransform(px / ptm, py / ptm, (float)(Math.random() * Math.PI));

		tempUserData = new userdata(false);
		tempUserData.objectID = 6;
		tempUserData.naturalFrequency = 430f;
		tempUserData.decayRate = 11.9652f;

		tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
		tempManager.objectBody.setBullet(true);

		fruitLayer.attachChild(tempManager.objectFace);
		tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
		this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);

		mScene.registerTouchArea(tempManager.objectFace);
		
		steelList.add(tempManager);
	}

	private void createButtons() {
		FixtureDef wallFixtureDef;		
		TiledTextureRegion tempRegion;

		wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0.5f);

		tempRegion = this.firstConditionTextureRegion.deepCopy();
		firstCondition = new TiledSprite(0, 0, tempRegion, this.getVertexBufferObjectManager());		
		firstCondition.setScale(0.25f);
		firstCondition.setRotationCenter(firstCondition.getWidthScaled() / 2, firstCondition.getHeightScaled() / 2);
		firstCondition.setPosition(-2.8f * firstCondition.getHeightScaled() , CAMERA_HEIGHT * 3f / 4f - firstCondition.getHeightScaled());
		firstConditionBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, firstCondition, BodyType.StaticBody, wallFixtureDef, ptm);						
		fruitLayer.attachChild(firstCondition);
		
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(firstCondition, firstConditionBody, true, true, ptm));
		mScene.registerTouchArea(firstCondition);

		
		tempRegion = this.wallObjectTextureRegion.deepCopy();
		firstWallCondition = new TiledSprite(0, 0, tempRegion, this.getVertexBufferObjectManager());		
		firstWallCondition.setScale(0.25f);
		firstWallCondition.setRotationCenter(firstWallCondition.getWidthScaled() / 2, firstWallCondition.getHeightScaled() / 2);
		firstWallCondition.setPosition(-0.2f * firstWallCondition.getHeightScaled(), CAMERA_HEIGHT * 3f / 4f - firstWallCondition.getHeightScaled());
		firstWallConditionBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, firstWallCondition, BodyType.StaticBody, wallFixtureDef, ptm);						
		fruitLayer.attachChild(firstWallCondition);
		
	
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(firstWallCondition, firstWallConditionBody, true, true, ptm));
		mScene.registerTouchArea(firstWallCondition);

		tempRegion = this.secondConditionTextureRegion.deepCopy();
		secondCondition = new TiledSprite(0,	0, tempRegion, this.getVertexBufferObjectManager());
		secondCondition.setScale(0.25f);
		secondCondition.setRotationCenter(firstCondition.getWidthScaled() / 2, firstCondition.getHeightScaled() / 2);
		secondCondition.setPosition(CAMERA_WIDTH / 3f - 2.8f * secondCondition.getHeightScaled(), CAMERA_HEIGHT * 3f / 4f - secondCondition.getHeightScaled());
		secondConditionBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, secondCondition, BodyType.StaticBody, wallFixtureDef, ptm);		
		fruitLayer.attachChild(secondCondition);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(secondCondition, secondConditionBody, true, true, ptm));
		mScene.registerTouchArea(secondCondition);
		
		tempRegion = this.wallObjectTextureRegion.deepCopy();
		secondWallCondition = new TiledSprite(0, 0, tempRegion, this.getVertexBufferObjectManager());		
		secondWallCondition.setScale(0.25f);
		secondWallCondition.setRotationCenter(secondWallCondition.getWidthScaled() / 2, secondWallCondition.getHeightScaled() / 2);
		secondWallCondition.setPosition(CAMERA_WIDTH / 3f + 0.2f * secondWallCondition.getHeightScaled(), CAMERA_HEIGHT * 3f / 4f - secondWallCondition.getHeightScaled());
		secondWallConditionBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, secondWallCondition, BodyType.StaticBody, wallFixtureDef, ptm);						
		fruitLayer.attachChild(secondWallCondition);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(secondWallCondition, secondWallConditionBody, true, true, ptm));
		mScene.registerTouchArea(secondWallCondition);

		
		
		tempRegion = this.thirdConditionTextureRegion.deepCopy();
		thirdCondition = new TiledSprite(0,	0, tempRegion, this.getVertexBufferObjectManager());
		thirdCondition.setScale(0.25f);
		thirdCondition.setRotationCenter(firstCondition.getWidthScaled() / 2, firstCondition.getHeightScaled() / 2);
		thirdCondition.setPosition(CAMERA_WIDTH * 2f / 3f - 1.3f * thirdCondition.getWidthScaled(), CAMERA_HEIGHT * 3f / 4f - thirdCondition.getHeightScaled());
		
		thirdConditionBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, thirdCondition, BodyType.StaticBody, wallFixtureDef, ptm);
		fruitLayer.attachChild(thirdCondition);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(thirdCondition, thirdConditionBody, true, true, ptm));
		mScene.registerTouchArea(thirdCondition);
		
		tempRegion = this.wallObjectTextureRegion.deepCopy();
		thirdWallCondition = new TiledSprite(0, 0, tempRegion, this.getVertexBufferObjectManager());		
		thirdWallCondition.setScale(0.25f);
		thirdWallCondition.setRotationCenter(thirdWallCondition.getWidthScaled() / 2, thirdWallCondition.getHeightScaled() / 2);
		thirdWallCondition.setPosition(CAMERA_WIDTH * 2f / 3f + 0.4f * thirdWallCondition.getHeightScaled(), CAMERA_HEIGHT * 3f / 4f - thirdWallCondition.getHeightScaled());
		thirdWallConditionBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, thirdWallCondition, BodyType.StaticBody, wallFixtureDef, ptm);						
		fruitLayer.attachChild(thirdWallCondition);
		
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(thirdWallCondition, thirdWallConditionBody, true, true, ptm));
		mScene.registerTouchArea(thirdWallCondition);
		
		
		return;
	}

	public boolean removeObjects() {
		int size = steelList.size();
		ObjectManager tempManager;
		
		for (int i = 0; i < size; i++) {
			tempManager = steelList.get(i);
			
			((userdata) tempManager.objectBody.getFixtureList().get(0).getUserData()).remove();
		}
		
		size = rubberList.size();
		for (int i = 0; i < size; i++) {
			tempManager = rubberList.get(i);
			
			((userdata) tempManager.objectBody.getFixtureList().get(0).getUserData()).remove();
		}
		
		size = woodList.size();
		for (int i = 0; i < size; i++) {
			tempManager = rubberList.get(i);
			
			((userdata) tempManager.objectBody.getFixtureList().get(0).getUserData()).remove();
		}
		
		return true;
	}
	
	public void resetObjects() {
		int size = steelList.size();
		ObjectManager tempManager;
		
		for (int i = 0; i < size; i++) {
			tempManager = steelList.get(i);
			
			mPhysicsWorld.unregisterPhysicsConnector(tempManager.objectConnector);
			tempManager.objectBody.setActive(false);
			mPhysicsWorld.destroyBody(tempManager.objectBody);
			fruitLayer.detachChild(tempManager.objectFace);
			mEngine.getScene().unregisterTouchArea(tempManager.objectFace);
		}
		
		size = rubberList.size();		
		
		for (int i = 0; i < size; i++) {
			tempManager = rubberList.get(i);
			
			mPhysicsWorld.unregisterPhysicsConnector(tempManager.objectConnector);
			tempManager.objectBody.setActive(false);
			mPhysicsWorld.destroyBody(tempManager.objectBody);
			fruitLayer.detachChild(tempManager.objectFace);
			mEngine.getScene().unregisterTouchArea(tempManager.objectFace);
		}
		
		size = woodList.size();		
		
		for (int i = 0; i < size; i++) {
			tempManager = woodList.get(i);
			
			mPhysicsWorld.unregisterPhysicsConnector(tempManager.objectConnector);
			tempManager.objectBody.setActive(false);
			mPhysicsWorld.destroyBody(tempManager.objectBody);
			fruitLayer.detachChild(tempManager.objectFace);
			mEngine.getScene().unregisterTouchArea(tempManager.objectFace);
		}
	}
	
	public void createGround() {
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();		

		// Create Concrete Block Ground
		Rectangle ground = new Rectangle(CAMERA_WIDTH / 3f / 2f, CAMERA_HEIGHT * 3f / 4f, CAMERA_WIDTH / 3f * 0.9f, 5, vertexBufferObjectManager);
		Rectangle roof = new Rectangle(CAMERA_WIDTH / 3f / 2f, 0f, CAMERA_WIDTH / 3f * 0.9f, 5, vertexBufferObjectManager);
		Rectangle left = new Rectangle(CAMERA_WIDTH / 3f * 0.05f, CAMERA_HEIGHT * 3f / 2f, 5f, CAMERA_HEIGHT * 3f / 4f, vertexBufferObjectManager);
		Rectangle right = new Rectangle(CAMERA_WIDTH / 3f / 2f + CAMERA_WIDTH / 3f * 0.45f, CAMERA_HEIGHT * 3f / 2f, 5f, CAMERA_HEIGHT * 3f / 4f, vertexBufferObjectManager);
		Rectangle tempRectangle = null;
		 
		TextureRegion groundRegion = this.mConcreteTextureRegion.deepCopy();
		
		FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0f, 0.688f, 0.5f);
		
		{
			// roof
			ObjectManager tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f / 2f, 0f, groundRegion, this.getVertexBufferObjectManager());
			userdata tempUserData = new userdata(false);
			tempUserData.objectID = 1;			
			tempUserData.objectType = ROOF;
			tempRectangle = roof;
			tempManager.objectFace.setWidth(CAMERA_WIDTH / 3f * 0.9f);
			tempManager.objectFace.setHeight(5f);
			tempManager.objectFace.setPosition(0, 0);
			
			tempUserData.naturalFrequency = 80f;		
			tempUserData.decayRate = 34.1268f;
			tempUserData.density = 1000f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);
			
			concreteGroundList.add(tempManager);
			
			// ground
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f / 2f, CAMERA_HEIGHT * 3f / 4, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 1;
			tempUserData.objectType = GROUND;
			tempRectangle = ground;
			tempManager.objectFace.setWidth(CAMERA_WIDTH / 3f * 0.9f);
			tempManager.objectFace.setHeight(5f);
			tempManager.objectFace.setPosition(0, CAMERA_HEIGHT * 3f / 4f);
			
			tempUserData.naturalFrequency = 80f;		
			tempUserData.decayRate = 34.1268f;
			tempUserData.density = 1000f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			concreteGroundList.add(tempManager);
			
			// left
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f * 0.05f, CAMERA_HEIGHT * 3f / 2f, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 1;
			tempUserData.objectType = LEFT;
			tempRectangle = left;
			tempManager.objectFace.setWidth(5f);
			tempManager.objectFace.setHeight(CAMERA_HEIGHT * 3f / 4f);
			tempManager.objectFace.setPosition(0f, 0f);
			
			tempUserData.naturalFrequency = 80f;		
			tempUserData.decayRate = 34.1268f;
			tempUserData.density = 1000f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			concreteGroundList.add(tempManager);
			
			// right
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f / 2f + CAMERA_WIDTH / 3f * 0.45f, CAMERA_HEIGHT * 3f / 2f, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 1;
			tempUserData.objectType = RIGHT;
			tempRectangle = right;
			tempManager.objectFace.setWidth(5f);
			tempManager.objectFace.setHeight(CAMERA_HEIGHT * 3f / 4f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH / 3f * 0.9f - 5f, 0f);
			
			tempUserData.naturalFrequency = 80f;		
			tempUserData.decayRate = 34.1268f;
			tempUserData.density = 1000f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			concreteGroundList.add(tempManager);
		}
					

		// Create Wooden Plate Ground
		groundRegion = this.mWoodTextureRegion.deepCopy();
		
		wallFixtureDef = PhysicsFactory.createFixtureDef(0f, 0.688f, 0.5f);
		
		{
			// roof
			ObjectManager tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f / 2f, 0f, groundRegion, this.getVertexBufferObjectManager());
			userdata tempUserData = new userdata(false);
			tempUserData.objectID = 2;
			tempUserData.objectType = ROOF;
			tempRectangle = roof;
			tempManager.objectFace.setWidth(CAMERA_WIDTH / 3f * 0.9f);
			tempManager.objectFace.setHeight(5f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f * 0.05f, 0);
			
			tempUserData.naturalFrequency = 152f;		
			tempUserData.decayRate = 106.9971f;
			tempUserData.density = 850f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);
			
			woodenGroundList.add(tempManager);
			
			// ground
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f / 2f, CAMERA_HEIGHT * 3f / 4, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 2;
			tempUserData.objectType = GROUND;
			tempRectangle = ground;
			tempManager.objectFace.setWidth(CAMERA_WIDTH / 3f * 0.9f);
			tempManager.objectFace.setHeight(5f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f * 0.05f, CAMERA_HEIGHT * 3f / 4f);
			
			tempUserData.naturalFrequency = 152f;		
			tempUserData.decayRate = 106.9971f;
			tempUserData.density = 850f;
			
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			woodenGroundList.add(tempManager);
			
			// left
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f * 0.05f, CAMERA_HEIGHT * 3f / 2f, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 2;
			tempUserData.objectType = LEFT;
			tempRectangle = left;
			tempManager.objectFace.setWidth(5f);
			tempManager.objectFace.setHeight(CAMERA_HEIGHT * 3f / 4f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f * 0.05f, 0);
			
			tempUserData.naturalFrequency = 152f;		
			tempUserData.decayRate = 106.9971f;
			tempUserData.density = 850f;
			
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			woodenGroundList.add(tempManager);
			
			// right
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f / 2f + CAMERA_WIDTH / 3f * 0.45f, CAMERA_HEIGHT * 3f / 2f, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 2;
			tempUserData.objectType = RIGHT;
			tempRectangle = right;
			tempManager.objectFace.setWidth(5f);
			tempManager.objectFace.setHeight(CAMERA_HEIGHT * 3f / 4f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH / 3f + CAMERA_WIDTH / 3f * 0.95f - 5f, 0f);			
			
			tempUserData.naturalFrequency = 152f;		
			tempUserData.decayRate = 106.9971f;
			tempUserData.density = 850f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			woodenGroundList.add(tempManager);
		}
			
		
		// Create Steel Plate Ground
		groundRegion = this.mSteelTextureRegion.deepCopy();
		
		wallFixtureDef = PhysicsFactory.createFixtureDef(0f, 0.688f, 0.5f);
		
		{
			// roof
			ObjectManager tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f / 2f, 0f, groundRegion, this.getVertexBufferObjectManager());
			userdata tempUserData = new userdata(false);
			tempUserData.objectID = 3;
			tempUserData.objectType = ROOF;
			tempRectangle = roof;
			tempManager.objectFace.setWidth(CAMERA_WIDTH / 3f * 0.9f);
			tempManager.objectFace.setHeight(5f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f * 0.1f, 0);
			
			tempUserData.naturalFrequency = 430f;		
			tempUserData.decayRate = 11.9652f;
			tempUserData.density = 1010f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);
			
			steelGroundList.add(tempManager);
			
			// ground
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f / 2f, CAMERA_HEIGHT * 3f / 4, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 3;
			tempUserData.objectType = GROUND;
			tempRectangle = ground;
			tempManager.objectFace.setWidth(CAMERA_WIDTH / 3f * 0.9f);
			tempManager.objectFace.setHeight(5f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f * 0.1f, CAMERA_HEIGHT * 3f / 4f);
			
			tempUserData.naturalFrequency = 430f;		
			tempUserData.decayRate = 11.9652f;
			tempUserData.density = 1010f;
			
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			steelGroundList.add(tempManager);
			
			// left
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f * 0.1f, CAMERA_HEIGHT * 3f / 2f, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 3;
			tempUserData.objectType = LEFT;
			tempRectangle = left;
			tempManager.objectFace.setWidth(5f);
			tempManager.objectFace.setHeight(CAMERA_HEIGHT * 3f / 4f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f * 0.1f, 0);
			
			tempUserData.naturalFrequency = 430f;		
			tempUserData.decayRate = 11.9652f;
			tempUserData.density = 1010f;
			
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			steelGroundList.add(tempManager);
			
			// right
			tempManager = new ObjectManager();
			
			tempManager.objectFace = new Sprite(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f / 2f + CAMERA_WIDTH / 3f * 0.45f, CAMERA_HEIGHT * 3f / 2f, groundRegion, this.getVertexBufferObjectManager());
			tempUserData = new userdata(false);
			tempUserData.objectID = 3;
			tempUserData.objectType = RIGHT;
			tempRectangle = right;
			tempManager.objectFace.setWidth(5f);
			tempManager.objectFace.setHeight(CAMERA_HEIGHT * 3f / 4f);
			tempManager.objectFace.setPosition(CAMERA_WIDTH * 2f / 3f + CAMERA_WIDTH / 3f - 5f, 0f);			
			
			tempUserData.naturalFrequency = 430f;		
			tempUserData.decayRate = 11.9652f;
			tempUserData.density = 1010f;
					
			tempUserData.calculateMass(tempRectangle.getWidthScaled(), tempRectangle.getHeightScaled(), 0, ptm);
			tempManager.objectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, tempManager.objectFace, BodyType.StaticBody, wallFixtureDef, ptm);
			tempManager.objectBody.getFixtureList().get(0).setUserData(tempUserData);
			
			fruitLayer.attachChild(tempManager.objectFace);		
			tempManager.objectConnector = new PhysicsConnector(tempManager.objectFace, tempManager.objectBody, true, true, ptm);
			this.mPhysicsWorld.registerPhysicsConnector(tempManager.objectConnector);			
			
			steelGroundList.add(tempManager);
		}		
	}
	
	
	public void initializePhysVib() {
		this.mPhysicsWorld.setContactListener(this.mContactListener);  
    	this.mContactListener.vibrationModule.setCurrentVibrationAPI(this.myOutput.getHandler());
    	
    	mContactListener.setPixelToMeterRatio(ptm);
		mContactListener.setCameraPixel(CAMERA_WIDTH);
		mContactListener.setAutomaticGain(true);
		mContactListener.thresholdImpulse = 0f;
	}
	
	@Override
	public Scene onCreateScene() {
		this.mScene = new Scene();		
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		ParallaxBackground background = new ParallaxBackground(0, 0, 0);
	    mScene.setBackground(background);
	    
	    fruitLayer = new Entity();
		scoreLayer = new Entity();
		fruitLayer.setPosition(0, 0);
		mScene.attachChild(fruitLayer);
		mScene.attachChild(scoreLayer);
	  
	    mScene.setOnSceneTouchListener(this);
	    mScene.setOnAreaTouchListener(this);

	    
	    
	    this.mPhysicsWorld = new FixedStepPhysicsWorld(100, new Vector2(0, SensorManager.GRAVITY_EARTH), false, 3, 2);	    	    
	    mPhysicsWorld.setContinuousPhysics(true);	    	    	    
	    mScene.registerUpdateHandler(this.mPhysicsWorld);	    
    
	    this.initializePhysVib();

	    createButtons();
		createGround();
		
				
		timerNumber = new Text(CAMERA_WIDTH / 2 - 300, 180, mFont, "", 300, new TextOptions(AutoWrap.NONE, 0, HorizontalAlign.LEFT, Text.LEADING_DEFAULT), getVertexBufferObjectManager());		
		timerNumber.setScaleCenter(timerNumber.getWidthScaled() / 2f, timerNumber.getHeightScaled() / 2f);
		timerNumber.setScale(0.3f);
		scoreLayer.attachChild(timerNumber);
		
		
		mEngine.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				// Update code for PhysVib.				
				elapsedTime += pSecondsElapsed;

				if (elapsedTime >= 1f / 100) {
					mContactListener.setTrigger();
					elapsedTime = 0;

					if (resetFlag) {
						resetFlag = false;

						removeObjects();
						resetObjects();
						
						mContactListener.checkMass();												
					}
				}
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}
		});
		
		resetFlag = true;
	    return this.mScene;			
	}

	
	@Override
	protected void onSetContentView() {
		this.setContentView(R.layout.main);
		this.mRenderSurfaceView = (RenderSurfaceView) findViewById(R.id.surfaceView);
		this.mRenderSurfaceView.setRenderer(this.mEngine, this);		
	}
	
	
	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		touchAction = pSceneTouchEvent.getAction();
		touchX = pSceneTouchEvent.getX();
		touchY = pSceneTouchEvent.getY();
		
		keepSprite = (TiledSprite)pTouchArea;
		
		final PhysicsConnector facePhysicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(keepSprite);
		BirdsBody = facePhysicsConnector.getBody();

		if (touchAction == TouchEvent.ACTION_UP) {
			if (BirdsBody != null) {
				if (BirdsBody.equals(firstConditionBody)) {
					// Concrete Block Active							
					boolean nextStatus = false;
					if (firstCondition.getCurrentTileIndex() == 0) { // Not focused, then change to focus
						nextStatus = true;
						firstCondition.setCurrentTileIndex(1);
						
						myOutput.vibMode = 1;
					}
					else {
						nextStatus = false;
						firstCondition.setCurrentTileIndex(0);
						
						myOutput.vibMode = 0;
					}
					
					if (firstWallCondition.getCurrentTileIndex() == 0) { // Wall Focus Change
						int size = concreteGroundList.size();
						
						userdata tempUserData;
						for (int i = 0; i < size; i++) {												
							tempUserData = (userdata)(concreteGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
							tempUserData.hapticCamera = nextStatus; 							
							concreteGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
						}																				
					}
					else {
						int size = rubberList.size();
						
						userdata tempUserData;
						for (int i = 0; i < size; i++) {											
							tempUserData = (userdata)(rubberList.get(i).objectBody.getFixtureList().get(0)).getUserData();
							tempUserData.hapticCamera = nextStatus; 							
							rubberList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
						}
					}					
				}
				else if (BirdsBody.equals(secondConditionBody)) {
					// Wooden Block Active
					boolean nextStatus = false;
					if (secondCondition.getCurrentTileIndex() == 0) { // Not focused, then change to focus
						nextStatus = true;
						secondCondition.setCurrentTileIndex(1);
						
						myOutput.vibMode = 2;
					}
					else {
						nextStatus = false;
						secondCondition.setCurrentTileIndex(0);
						
						myOutput.vibMode = 0;
					}
					
					if (secondWallCondition.getCurrentTileIndex() == 0) { // Wall Focus Change
						int size = woodenGroundList.size();
						
						userdata tempUserData;
						for (int i = 0; i < size; i++) {												
							tempUserData = (userdata)(woodenGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
							tempUserData.hapticCamera = nextStatus; 							
							woodenGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
						}																				
					}
					else {
						int size = woodList.size();
						
						userdata tempUserData;
						for (int i = 0; i < size; i++) {												
							tempUserData = (userdata)(woodList.get(i).objectBody.getFixtureList().get(0)).getUserData();
							tempUserData.hapticCamera = nextStatus; 							
							woodList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
						}
					}																			
				}
				else if (BirdsBody.equals(thirdConditionBody)) {
					// Steel Block Active
					boolean nextStatus = false;
					if (thirdCondition.getCurrentTileIndex() == 0) { // Not focused, then change to focus
						nextStatus = true;
						thirdCondition.setCurrentTileIndex(1);
					}
					else {
						nextStatus = false;
						thirdCondition.setCurrentTileIndex(0);
					}
					
					if (thirdWallCondition.getCurrentTileIndex() == 0) { // Wall Focus Change
						int size = steelGroundList.size();
						
						userdata tempUserData;
						for (int i = 0; i < size; i++) {												
							tempUserData = (userdata)(steelGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
							tempUserData.hapticCamera = nextStatus; 							
							steelGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
						}																				
					}
					else {
						int size = steelList.size();
						
						userdata tempUserData;
						for (int i = 0; i < size; i++) {												
							tempUserData = (userdata)(steelList.get(i).objectBody.getFixtureList().get(0)).getUserData();
							tempUserData.hapticCamera = nextStatus; 							
							steelList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
						}
					}															
				}
				else if (BirdsBody.equals(firstWallConditionBody)) {
					boolean nextStatus = false;
					if (firstCondition.getCurrentTileIndex() == 1) {
						if (firstWallCondition.getCurrentTileIndex() == 0) { // Focus changes from wall to objects
							firstWallCondition.setCurrentTileIndex(1);
							
							int size = concreteGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(concreteGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								concreteGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = rubberList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(rubberList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = true; 							
								rubberList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								rubberList.get(i).objectBody.getFixtureList().get(0).setDensity(120f);
								rubberList.get(i).objectBody.resetMassData();
							}							
						}
						else {
							firstWallCondition.setCurrentTileIndex(0);
							int size = concreteGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(concreteGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = true; 							
								concreteGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = rubberList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(rubberList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								rubberList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								rubberList.get(i).objectBody.getFixtureList().get(0).setDensity(120f);
								rubberList.get(i).objectBody.resetMassData();
							}														
						}
					}			
					else if (firstCondition.getCurrentTileIndex() == 0) {
						if (firstWallCondition.getCurrentTileIndex() == 0) { // Focus changes from wall to objects
							firstWallCondition.setCurrentTileIndex(1);
							
							int size = concreteGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(concreteGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								concreteGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = rubberList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(rubberList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								rubberList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								rubberList.get(i).objectBody.getFixtureList().get(0).setDensity(120f);
								rubberList.get(i).objectBody.resetMassData();
							}							
						}
						else {
							firstWallCondition.setCurrentTileIndex(0);
							int size = concreteGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(concreteGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								concreteGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = rubberList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(rubberList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								rubberList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								rubberList.get(i).objectBody.getFixtureList().get(0).setDensity(120f);
								rubberList.get(i).objectBody.resetMassData();
							}														
						}
					}		
				}
				else if (BirdsBody.equals(secondWallConditionBody)) {
					boolean nextStatus = false;
					if (secondCondition.getCurrentTileIndex() == 1) {
						if (secondWallCondition.getCurrentTileIndex() == 0) { // Focus changes from wall to objects
							secondWallCondition.setCurrentTileIndex(1);
							
							int size = woodenGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodenGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								woodenGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = woodList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = true; 							
								woodList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								woodList.get(i).objectBody.getFixtureList().get(0).setDensity(850f);
								woodList.get(i).objectBody.resetMassData();
							}							
						}
						else {
							
							secondWallCondition.setCurrentTileIndex(0);

							int size = woodenGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodenGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = true; 							
								woodenGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = woodList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								woodList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								woodList.get(i).objectBody.getFixtureList().get(0).setDensity(850f);
								woodList.get(i).objectBody.resetMassData();
							}														
						}
					}				
					else if (secondCondition.getCurrentTileIndex() == 0) {
						if (secondWallCondition.getCurrentTileIndex() == 0) { // Focus changes from wall to objects
							secondWallCondition.setCurrentTileIndex(1);
							
							int size = woodenGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodenGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								woodenGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = woodList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								woodList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								woodList.get(i).objectBody.getFixtureList().get(0).setDensity(850f);
								woodList.get(i).objectBody.resetMassData();
							}							
						}
						else {
							
							secondWallCondition.setCurrentTileIndex(0);

							int size = woodenGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodenGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								woodenGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = woodList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(woodList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								woodList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								woodList.get(i).objectBody.getFixtureList().get(0).setDensity(850f);
								woodList.get(i).objectBody.resetMassData();
							}														
						}
					}						
				}	
				else if (BirdsBody.equals(thirdWallConditionBody)) {
					boolean nextStatus = false;
					if (thirdCondition.getCurrentTileIndex() == 1) {
						if (thirdWallCondition.getCurrentTileIndex() == 0) { // Focus changes from wall to objects
							thirdWallCondition.setCurrentTileIndex(1);
							
							int size = steelGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								steelGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = steelList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = true; 							
								steelList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								steelList.get(i).objectBody.getFixtureList().get(0).setDensity(1193f);
								steelList.get(i).objectBody.resetMassData();
							}							
						}
						else {
							thirdWallCondition.setCurrentTileIndex(0);

							int size = steelGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = true; 							
								steelGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = steelList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								steelList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								steelList.get(i).objectBody.getFixtureList().get(0).setDensity(1193f);
								steelList.get(i).objectBody.resetMassData();
							}														
						}
					}						
					else if (thirdCondition.getCurrentTileIndex() == 0) {
						if (thirdWallCondition.getCurrentTileIndex() == 0) { // Focus changes from wall to objects
							thirdWallCondition.setCurrentTileIndex(1);
							
							int size = steelGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								steelGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = steelList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								steelList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								steelList.get(i).objectBody.getFixtureList().get(0).setDensity(1193f);
								steelList.get(i).objectBody.resetMassData();
							}							
						}
						else {
							thirdWallCondition.setCurrentTileIndex(0);

							int size = steelGroundList.size();
							
							userdata tempUserData;
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelGroundList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								steelGroundList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
							}							
						
							size = steelList.size();
														
							for (int i = 0; i < size; i++) {													
								tempUserData = (userdata)(steelList.get(i).objectBody.getFixtureList().get(0)).getUserData();
								tempUserData.hapticCamera = false; 							
								steelList.get(i).objectBody.getFixtureList().get(0).setUserData(tempUserData);
								steelList.get(i).objectBody.getFixtureList().get(0).setDensity(1193f);
								steelList.get(i).objectBody.resetMassData();
							}														
						}
					}							
				}
				else {
					int index = findBody(BirdsBody, 0);					
					ObjectManager tempManager = null;
					if (index != -1) {
						tempManager = rubberList.get(index);
						
						((userdata) tempManager.objectBody.getFixtureList().get(0).getUserData()).remove();
						mPhysicsWorld.unregisterPhysicsConnector(tempManager.objectConnector);
						tempManager.objectBody.setActive(false);
						mPhysicsWorld.destroyBody(tempManager.objectBody);
						fruitLayer.detachChild(tempManager.objectFace);
						mEngine.getScene().unregisterTouchArea(tempManager.objectFace);		
						
						rubberList.remove(index);
					}
					
					index = findBody(BirdsBody, 1);					
					
					if (index != -1) {
						tempManager = woodList.get(index);

						((userdata) tempManager.objectBody.getFixtureList().get(0).getUserData()).remove();
						mPhysicsWorld.unregisterPhysicsConnector(tempManager.objectConnector);
						tempManager.objectBody.setActive(false);
						mPhysicsWorld.destroyBody(tempManager.objectBody);
						fruitLayer.detachChild(tempManager.objectFace);
						mEngine.getScene().unregisterTouchArea(tempManager.objectFace);		
						
						woodList.remove(index);
					}
					
					index = findBody(BirdsBody, 2);					
					
					if (index != -1) {
						tempManager = steelList.get(index);

						((userdata) tempManager.objectBody.getFixtureList().get(0).getUserData()).remove();
						mPhysicsWorld.unregisterPhysicsConnector(tempManager.objectConnector);
						tempManager.objectBody.setActive(false);
						mPhysicsWorld.destroyBody(tempManager.objectBody);
						fruitLayer.detachChild(tempManager.objectFace);
						mEngine.getScene().unregisterTouchArea(tempManager.objectFace);		
						
						steelList.remove(index);
					}
				}
			}			
		}
		
		return true;
	}

	public void onLoadComplete() {

	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		touchAction = pSceneTouchEvent.getAction();
		touchX = pSceneTouchEvent.getX();
		touchY = pSceneTouchEvent.getY();
		
		if (touchAction == TouchEvent.ACTION_DOWN) {										
			anchorX = touchX;
			anchorY = touchY;
		}
		else if (touchAction == TouchEvent.ACTION_UP || touchAction == TouchEvent.ACTION_OUTSIDE) {
			springX = touchX;
			springY = touchY;			
		}
		
		if (currentMenu == MENU_EXPERIMENT) {			
			if (touchAction == TouchEvent.ACTION_UP || touchAction == TouchEvent.ACTION_OUTSIDE) {
				
				
				Vector2 tempVector = new Vector2(springX - anchorX,springY - anchorY);
				
				if (tempVector.len2() <= 50f) {
					// Slightly moves -> just touched the screen
					if (BirdsBody != null) {
						
					}
					else if (BirdsBody == null) {						
						if (touchX < CAMERA_WIDTH / 3f && touchY < CAMERA_HEIGHT * 3f / 4f) {
							createRubberBall(touchX, touchY);							
						}
						else if (touchX < CAMERA_WIDTH * 2f / 3f && touchY < CAMERA_HEIGHT * 3f / 4f) {
							createWoodenBall(touchX, touchY);							
						}
						else if (touchY < CAMERA_HEIGHT * 3f / 4f) {
							createSteelCan(touchX, touchY);							
						}						
					}
				}				
				else {				
					if (BirdsBody != null) {
						//timerNumber.setText("T: " + tiltCount[experimentIndex] + " D: " + dragCount[experimentIndex]);
						BirdsBody.applyLinearImpulse(tempVector.mul(100f), new Vector2(anchorX, anchorY).mul(1/ptm));						
					}					
				}
				BirdsBody = null;
				keepSprite = null;

				anchorX = 0;
				anchorY = 0;
				springX = 0;
				springY = 0;				
			}
		}
		return false;
	}

	
	
	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerometerData) {
		
		mTempVector.set(pAccelerometerData.getX(), pAccelerometerData.getY());
		float vectorMagnitude = mTempVector.len();
		mTempVector.nor();		
		mTempVector3[2] = pAccelerometerData.getZ();
		mTempVector3[0] = mTempVector.x;
		mTempVector3[1] = mTempVector.y;	
		
		this.mTempVector.mul(9.8f);
		this.mPhysicsWorld.setGravity(this.mTempVector);
	}

	
	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		return super.onCreateOptionsMenu(pMenu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {		
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public boolean onMenuItemSelected(final int pFeatureId, final MenuItem pItem) {
		switch(pItem.getItemId()) {
			default:
				return super.onMenuItemSelected(pFeatureId, pItem);
		}
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onResume() {
	    // TODO Auto-generated method stub
	    super.onResume();
	}


	@Override
	protected void onPause() {
	    // TODO Auto-generated method stub
	    super.onPause();
	}
	
	
	int findBody(Body inBody, int list) {
		int size = 0;
		ObjectManager tempManager;
		
		if (list == 0) { // rubber
			size = rubberList.size();
			
			for (int i = 0; i < size; i++) {
				tempManager = rubberList.get(i);
				if (tempManager.objectBody.equals(inBody))
					return i;
			}
		}
		else if (list== 1) { // wood
			size = woodList.size();
			
			for (int i = 0; i < size; i++) {
				tempManager = woodList.get(i);
				if (tempManager.objectBody.equals(inBody))
					return i;
			}
		}
		else if (list== 2) { // steel
			size = steelList.size();
			
			for (int i = 0; i < size; i++) {
				tempManager = steelList.get(i);
				if (tempManager.objectBody.equals(inBody))
					return i;
			}
		}
		
		return -1;
	}
	
	class ObjectManager {
		Sprite objectFace;
		Body objectBody;
		PhysicsConnector objectConnector;
	}
}

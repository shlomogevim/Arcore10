package com.example.arcore10

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.arcore10.utils.RotatingNode
import com.example.arcore10.utils.Util
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {
    //bee_drill selector = 1   onePlaceNode=false
    // golden_fish selector=2 fireBaseSource=true  onePlaceNode=true
    //earth_ball selector = 3   onePlaceNode=true
    //dancer selector = 4   onePlaceNode=true

    val selector = 2
    val onePlaceNode = true
    var fireBaseSource=false
    var maxModelScale = 0.07f
    var minModelScale = 0.06f


    lateinit var model: Models
    var modelResourceId = 1
    var animationSring = ""
    var url=""

    private lateinit var util: Util
    private val nodes = mutableListOf<RotatingNode>()
    private val viewNodes = mutableListOf<Node>()
    private lateinit var arFragment: ArFragment
    private var curCameraPosition = Vector3.zero()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSelector()
        arFragment = fragment1 as ArFragment
        util = Util(this, arFragment)

        arFragment.setOnTapArPlaneListener { hitResult, _,_ ->
            spawnObject(hitResult.createAnchor(), Uri.parse(url))

        }

      /*  arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if (onePlaceNode) {
                // arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
                loadModel { modelRenderable, viewRenderable ->
                    addNodeToScence(hitResult.createAnchor(), modelRenderable, viewRenderable)
                }
                // }

            } else {
                loadModelAndAddToSceneRound(hitResult.createAnchor(), modelResourceId)
            }

        }
        arFragment.arSceneView.scene.addOnUpdateListener {
            updateNodes()
        }
        util.activateButtom()*/
    }

    private fun setSelector() {
        when (selector) {
            1 -> {
                model = Models.Bee
                modelResourceId = R.raw.beedrill
                animationSring = "Beedrill_Animation"
            }
            2->{
                model=Models.Fish
                url = "https://firebasestorage.googleapis.com/v0/b/thermal-proton-239415.appspot.com/o/golde_fish.glb?alt=media&token=04c99487-dea7-48a0-96f5-05a2f1fed3d5"

            }
            3 -> modelResourceId = R.raw.earth_ball
            4 -> {
                model = Models.Dance
                modelResourceId = R.raw.biomutantdance_motionplus0
                animationSring = "motionplus0"
                maxModelScale = 0.07f
                minModelScale = 0.06f
            }

        }

    }
    private fun spawnObject(anchor: Anchor,modelUri:Uri){
        val rendrebaleSource= RenderableSource.builder()
            .setSource(this,modelUri, RenderableSource.SourceType.GLB)
            .setScale(0.01f)
            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
            .build()
        ModelRenderable.builder()
            .setSource(this,rendrebaleSource)
            .setRegistryId(modelUri)
            .build()
            .thenAccept {
                addNodeToScene(anchor,it)
            }.exceptionally {
                Log.e("clima","Somthing go wrong in loading model")
                null
            }
    }
    private fun addNodeToScene(anchor: Anchor,modelRenderable: ModelRenderable){
        val anchorNode=AnchorNode(anchor)
        TransformableNode(arFragment.transformationSystem).apply {
            renderable=modelRenderable
            setParent(anchorNode)
        }
        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    /*
    *  override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            spawnObject(hitResult.createAnchor(), Uri.parse(url))

        }
        arFragment.arSceneView.scene.addOnUpdateListener {
            updateNodes()
        }
    }

    private fun spawnObject(anchor: Anchor, modelUri: Uri) {
          val rendrebaleSource = RenderableSource.builder()
              .setSource(this, modelUri, RenderableSource.SourceType.GLB)
              .setScale(0.002f)
              // .setScale(0.002f)
              .setRecenterMode(RenderableSource.RecenterMode.ROOT)
              .build()
          ModelRenderable.builder()
              .setSource(this, rendrebaleSource)
              .setRegistryId(modelUri)
              .build()
              .thenAccept {
                  addNotesToScene1(anchor, it)
              }.exceptionally {
                  Log.e("clima", "Somthing go wrong in loading model")
                  null
              }
      }


      private fun addNotesToScene1(anchor: Anchor, modelRenderable: ModelRenderable) {
          val anchorNode = AnchorNode(anchor)
          val rotatingNode = RotatingNode(model.degreesPerSecond).apply {
              setParent(anchorNode)
          }
          Node().apply {
              renderable = modelRenderable
              setParent(rotatingNode)
            //  localPosition = Vector3(model.radius, model.height, 0f)
             // localRotation = Quaternion.eulerAngles(Vector3(0f, model.rotationDegrees, 0f))
              arFragment.arSceneView.scene.addChild(anchorNode)
              nodes.add(rotatingNode)
              val animateData = modelRenderable.getAnimationData("Beedrill_Animation")
              ModelAnimator(animateData, modelRenderable).apply {
                  repeatCount = ModelAnimator.INFINITE
                  start()
              }

          }
      }*/

    private fun loadModel(callback: (ModelRenderable, ViewRenderable) -> Unit) {
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, modelResourceId)
            .build()
        val viewRenderable = ViewRenderable.builder()
            .setView(this, createDeleteButton())
            .build()
        CompletableFuture.allOf(modelRenderable, viewRenderable)
            .thenAccept {
                callback(modelRenderable.get(), viewRenderable.get())
            }
            .exceptionally {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun createDeleteButton(): Button {  //  you can creat any view even entire layout
        return Button(this).apply {
            text = "  סתם הודעה  "
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
        }
    }

    private fun addNodeToScence(     // for stay in one place
        anchor: Anchor,
        modelRenderable: ModelRenderable,
        viewRenderable: ViewRenderable
    ) {
        val anchorNode = AnchorNode(anchor)
        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            renderable = modelRenderable
            scaleController.maxScale = maxModelScale
            scaleController.minScale = minModelScale
            setParent(anchorNode)
            getCurrentScene().addChild(anchorNode)
            // select()
            startAnimation(renderable as ModelRenderable)
        }
        //addViewAbove(anchorNode,modelNode,viewRenderable)
    }

    private fun startAnimation(renderable: ModelRenderable) {
        if (renderable.animationDataCount == 0) {
            return
        }
        val animationData = renderable.getAnimationData(animationSring)
        ModelAnimator(animationData, renderable).apply {
            repeatCount = ModelAnimator.INFINITE
            start()
        }

    }

    private fun addViewAbove(
        anchorNode: AnchorNode,
        modelNode: TransformableNode,
        viewRenderable: ViewRenderable
    ) {
        val viewNode = Node().apply {
            renderable = null // not seen at first
            setParent(modelNode)
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y, 0f)  //x,y,z
            (viewRenderable.view as Button).setOnClickListener {   // fine definition to buttom id
                getCurrentScene().removeChild(anchorNode)           // remove the model amd the view
                viewNodes.remove(this)
            }
        }
        viewNodes.add(viewNode)
        modelNode.setOnTapListener { _, _ ->
            if (!modelNode.isTransforming) {  // if the model is not movving
                if (viewNode.renderable == null) {
                    viewNode.renderable = viewRenderable
                } else {
                    viewNode.renderable = null
                }
            }
        }

    }

    private fun getCurrentScene() = arFragment.arSceneView.scene


    private fun loadModelAndAddToSceneRound(anchor: Anchor, modelResourceId: Int) {
        ModelRenderable.builder()
            .setSource(this, modelResourceId)
            .build()
            .thenAccept { modelRenderable ->
                addNodeToSceneRound(anchor, modelRenderable, model)
                //util.eliminateDot()
            }.exceptionally {
                Toast.makeText(this, "Error creating node: $it", Toast.LENGTH_LONG).show()
                null
            }
    }

    private fun updateNodes() {
        curCameraPosition = arFragment.arSceneView.scene.camera.worldPosition
        for (node in nodes) {
            node.worldPosition =
                Vector3(curCameraPosition.x, node.worldPosition.y, curCameraPosition.z)
        }
    }

    private fun addNodeToSceneRound(
        anchor: Anchor,
        modelRenderable: ModelRenderable,
        spaceship: Models
    ) {
        val anchorNode = AnchorNode(anchor)
        val rotatingNode = RotatingNode(spaceship.degreesPerSecond)
            .apply {
                setParent(anchorNode)
            }
        Node().apply {
            renderable = modelRenderable
            setParent(rotatingNode)
            localPosition = Vector3(spaceship.radius, spaceship.height, 0f)
            localRotation = Quaternion.eulerAngles(Vector3(0f, spaceship.rotationDegrees, 0f))
        }
        arFragment.arSceneView.scene.addChild(anchorNode)
        nodes.add(rotatingNode)
        val animationData = modelRenderable.getAnimationData(animationSring)
        ModelAnimator(animationData, modelRenderable).apply {
            repeatCount = ModelAnimator.INFINITE
            start()
        }
    }

}




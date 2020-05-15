package com.example.arcore10

import android.media.CamcorderProfile
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.example.arcore10.utils.PhotoSaver
import com.example.arcore10.utils.RotatingNode
import com.example.arcore10.utils.Util
import com.example.arcore10.utils.VideoRecorder
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val selector = 2



    lateinit var spaceship: Models
    var modelResourceId = 1
    var animationSring = ""


    private lateinit var util: Util
    private val nodes = mutableListOf<RotatingNode>()
    private lateinit var arFragment: ArFragment
    private var curCameraPosition = Vector3.zero()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSelector()
        arFragment = fragment1 as ArFragment
        util = Util(this, arFragment)

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            loadModelAndAddToScene(hitResult.createAnchor(), modelResourceId)
        }
        arFragment.arSceneView.scene.addOnUpdateListener {
            updateNodes()
        }
        util.activateButtom()
    }

    private fun setSelector() {
        when (selector) {
            1 -> {
                spaceship = Models.Bee
                modelResourceId = R.raw.beedrill
                animationSring = "Beedrill_Animation"
            }
            2->{
                    spaceship = Models.Dance
                   modelResourceId = R.raw.biomutantdance_motionplus0
               // animationSring = "biomutantdance_motionplus0"
                animationSring = "motionplus0"
            }
        }

    }


    private fun loadModelAndAddToScene(anchor: Anchor, modelResourceId: Int) {
        ModelRenderable.builder()
            .setSource(this, modelResourceId)
            .build()
            .thenAccept { modelRenderable ->
                addNodeToScene(anchor, modelRenderable, spaceship)
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

    private fun addNodeToScene(
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




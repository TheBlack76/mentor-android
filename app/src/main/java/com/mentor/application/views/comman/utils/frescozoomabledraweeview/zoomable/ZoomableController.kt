package com.mentor.application.views.comman.utils.frescozoomabledraweeview.zoomable

import android.graphics.Matrix
import android.graphics.RectF
import android.view.MotionEvent

/**
 * Interface for implementing a controller that works with [ZoomableDraweeView]
 * to control the zoom.
 */
interface ZoomableController {

    /**
     * Gets whether the controller is enabled. This should return the last value passed to
     * [.setEnabled].
     *
     * @return whether the controller is enabled.
     */
    /**
     * Enables the controller. The controller is enabled when the image has been loaded.
     *
     * @param enabled whether to enable the controller
     */
    var isEnabled: Boolean

    /**
     * Gets the current scale factor. A convenience method for calculating the scale from the
     * transform.
     *
     * @return the current scale factor
     */
    val scaleFactor: Float

    /**
     * Returns true if the zoomable transform is identity matrix, and the controller is idle.
     */
    val isIdentity: Boolean

    /**
     * Gets the current transform.
     *
     * @return the transform
     */
    val transform: Matrix

    /**
     * Listener interface.
     */
    interface Listener {

        /**
         * Notifies the view that the transform changed.
         *
         * @param transform the new matrix
         */
        fun onTransformChanged(transform: Matrix)
    }

    /**
     * Sets the listener for the controller to call back when the matrix changes.
     *
     * @param listener the listener
     */
    fun setListener(listener: Listener)

    /**
     * Returns true if the transform was corrected during the last update.
     *
     * This mainly happens when a gesture would cause the image to get out of limits and the
     * transform gets corrected in order to prevent that.
     */
    fun wasTransformCorrected(): Boolean

    /**
     * See [android.support.v4.view.ScrollingView].
     */
    fun computeHorizontalScrollRange(): Int

    fun computeHorizontalScrollOffset(): Int
    fun computeHorizontalScrollExtent(): Int
    fun computeVerticalScrollRange(): Int
    fun computeVerticalScrollOffset(): Int
    fun computeVerticalScrollExtent(): Int

    /**
     * Sets the bounds of the image post transform prior to application of the zoomable
     * transformation.
     *
     * @param imageBounds the bounds of the image
     */
    fun setImageBounds(imageBounds: RectF)

    /**
     * Sets the bounds of the view.
     *
     * @param viewBounds the bounds of the view
     */
    fun setViewBounds(viewBounds: RectF)

    /**
     * Allows the controller to handle a touch event.
     *
     * @param event the touch event
     * @return whether the controller handled the event
     */
    fun onTouchEvent(event: MotionEvent): Boolean
}

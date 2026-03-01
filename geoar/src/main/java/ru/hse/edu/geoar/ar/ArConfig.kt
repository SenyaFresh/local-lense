package ru.hse.edu.geoar.ar

import com.google.ar.core.Config

data class ArConfig(
    var planeFindingMode: Config.PlaneFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL,

    var lightEstimationMode: Config.LightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR,

    var depthEnabled: Boolean = true,

    var instantPlacementEnabled: Boolean = false,

    var arRequired: Boolean = true,

    var focusMode: Config.FocusMode = Config.FocusMode.AUTO,

    var updateMode: Config.UpdateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
)
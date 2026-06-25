using UnityEngine;

/// <summary>
/// Makes a World Space Canvas usable in VR by keeping it positioned in front of
/// the active VR camera and assigning that camera as the canvas event camera.
///
/// Screen Space - Overlay canvases do NOT render to an HMD, so the HUD/pause UI
/// must be World Space. Because the VR camera lives on a runtime-spawned player
/// prefab, this component locates Camera.main each frame until it appears, then
/// smoothly follows it like a heads-up display.
/// </summary>
[RequireComponent(typeof(Canvas))]
public class VRWorldUIFollow : MonoBehaviour
{
    [Tooltip("Distance in meters the panel sits in front of the camera.")]
    public float distance = 2.0f;

    [Tooltip("Vertical offset in meters (negative = below eye line).")]
    public float verticalOffset = -0.25f;

    [Tooltip("How quickly the panel catches up to the head. Higher = snappier. 0 = hard lock.")]
    public float followSpeed = 8.0f;

    [Tooltip("If true the panel only re-centers when the GameObject becomes active, then holds world position (good for pause menus). If false it follows continuously (good for a HUD).")]
    public bool recenterOnEnableOnly = false;

    private Canvas canvas;
    private Camera targetCamera;
    private bool placed;

    private void Awake()
    {
        canvas = GetComponent<Canvas>();
        if (canvas != null && canvas.renderMode != RenderMode.WorldSpace)
        {
            canvas.renderMode = RenderMode.WorldSpace;
        }
    }

    private void OnEnable()
    {
        placed = false;
    }

    private void LateUpdate()
    {
        if (targetCamera == null)
        {
            targetCamera = Camera.main;
            if (targetCamera == null)
            {
                return;
            }

            if (canvas != null)
            {
                canvas.worldCamera = targetCamera;
            }
        }

        if (recenterOnEnableOnly && placed)
        {
            return;
        }

        Transform cam = targetCamera.transform;
        Vector3 forwardFlat = cam.forward;
        Vector3 targetPos = cam.position + forwardFlat * distance + Vector3.up * verticalOffset;
        Quaternion targetRot = Quaternion.LookRotation(transform.position - cam.position, Vector3.up);

        if (!placed || recenterOnEnableOnly || followSpeed <= 0f)
        {
            transform.position = targetPos;
            transform.rotation = Quaternion.LookRotation(targetPos - cam.position, Vector3.up);
        }
        else
        {
            float t = 1f - Mathf.Exp(-followSpeed * Time.deltaTime);
            transform.position = Vector3.Lerp(transform.position, targetPos, t);
            transform.rotation = Quaternion.Slerp(transform.rotation, targetRot, t);
        }

        placed = true;
    }
}

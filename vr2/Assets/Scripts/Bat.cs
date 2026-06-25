using UnityEngine;
using UnityEngine.XR.Interaction.Toolkit;

public class Bat : MonoBehaviour
{
    public bool grabbed;
    public bool stayInHand;
    public GameObject gameManagerObject;

    public XRGrabInteractable grabInteractable;

    private bool everGrabbed;

    public void OnGrab()
    {
        grabbed = true;
        everGrabbed = true;
        gameManagerObject.GetComponent<GameManager>().StartGame();
    }


    public void OnRelease()
    {
        grabbed = false;
    }
}

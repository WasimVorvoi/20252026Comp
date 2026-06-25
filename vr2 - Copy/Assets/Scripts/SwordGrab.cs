using UnityEngine;
using UnityEngine.XR.Interaction.Toolkit;

public class SwordGrab : MonoBehaviour
{
    public Score score;
    bool gameStarted = false;

    void Start()
    {
        score = FindObjectOfType<Score>();
        GetComponent<XRGrabInteractable>().selectEntered.AddListener(OnGrabbed);
    }

    void OnGrabbed(SelectEnterEventArgs args)
    {
        if (gameStarted) return;
        gameStarted = true;
        score.StartGame();
    }
}

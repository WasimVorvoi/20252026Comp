using UnityEngine;
using UnityEngine.UI;

public class SpectatorScreen : MonoBehaviour
{
    public GameObject cameraPrefab;
    public RawImage screen;

    private GameObject currentCamera;
    private int currentIndex;

    public void ShowNext()
    {
        Destroy(currentCamera);

        DojoPlayer[] all = FindObjectsOfType<DojoPlayer>();
        for (int i = 0; i < all.Length; i++)
        {
            currentIndex = (currentIndex + 1) % all.Length;
            if (all[currentIndex].isLocalPlayer == false)
            {
                GameObject cam = Instantiate(cameraPrefab);
                cam.transform.SetParent(all[currentIndex].transform, false);
                currentCamera = cam;
                screen.gameObject.SetActive(true);
                return;
            }
        }
    }
}

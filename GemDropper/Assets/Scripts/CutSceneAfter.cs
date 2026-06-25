using UnityEngine;
using UnityEngine.SceneManagement;

public class CutSceneAfter : MonoBehaviour
{
    public string clipName;

    Animation anim;
    bool done;

    void Start()
    {
        anim = GetComponent<Animation>();
        anim.Play(clipName);
    }

    void Update()
    {
        if (done) return;

        if (!anim.isPlaying)
        {
            done = true;
            SceneManager.LoadScene(1);
        }
    }
}

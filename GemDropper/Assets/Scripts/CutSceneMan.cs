using UnityEngine;
using UnityEngine.SceneManagement;

public class CutSceneMan : MonoBehaviour
{
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        if (Input.GetKeyDown(KeyCode.Space))
        {
            scene1();
        }
    }
    public void scene1()
    {
        SceneManager.LoadScene(2);
    }
}

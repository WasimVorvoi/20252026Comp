using UnityEngine;
using UnityEngine.SceneManagement;

public class StatrMan : MonoBehaviour
{
    // Start is called once before the first execution of Update after the MonoBehaviour is created

    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    public void scene()
    {
        SceneManager.LoadScene(1);
    }
    public void quit()
    {
        Application.Quit();
    }
    public void menu()
    {
        SceneManager.LoadScene(0);
    }   
}

using UnityEngine;

public class ScriptSun : MonoBehaviour
{
    private int speed;
    public int Speed
    {
        get { return speed; }
        set { speed = value; }
    }
    void Start()
    {
        speed = 50;
    }

    void Update()
    {
        if (Input.GetKey(KeyCode.UpArrow))
        {
            speed += 10;
            Debug.Log(speed);
        }
        if (Input.GetKey(KeyCode.DownArrow))
        {
            speed -= 10;
            if (speed < 0)
            {
                speed = 0;
            }
            Debug.Log(speed);
        }
        if (Input.GetKey(KeyCode.LeftArrow))
        {
            transform.Rotate(Vector3.left * Time.deltaTime * Speed, Space.World);
        }
        if (Input.GetKey(KeyCode.RightArrow))
        {
            transform.Rotate(Vector3.right * Time.deltaTime * Speed, Space.World);
        }
        
    }
}

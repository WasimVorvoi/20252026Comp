using UnityEngine;

public class Rotate : MonoBehaviour
{
    private float speed;
    public float Speed
    {
        get { return speed; }
        set { speed = value; }
    }
    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        speed = 50;
    }

    // Update is called once per frame
    void Update()
    {
        
    }
    public void FixedUpdate()
    {
        if (Input.GetKey(KeyCode.A))
        {
            transform.Rotate(Vector3.up * Time.deltaTime * Speed, Space.World);
        }
        if(Input.GetKey(KeyCode.D))
        {
            transform.Rotate(Vector3.down * Time.deltaTime * Speed, Space.World);

        }
}
}

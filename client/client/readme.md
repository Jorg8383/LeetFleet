# Client

## Building docker image

1. cd into this directory
2. `docker build . -t client`

The main docker-compose file in the project directory will run the image, But if you'd like to run it on it's own you can run
`docker run -p 80:8000 client`

## Running locally

1. cd into this directory where manage.py is.
2. `python manage.py runserver`
3. go to localhost:8000

You will need Python, Django and Django Cors installed to run the file. The API calls within the webpage will not work without the akka system running.

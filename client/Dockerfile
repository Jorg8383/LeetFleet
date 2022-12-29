FROM python:3.9-buster

RUN  mkdir /app
ENV PYTHONUNBUFFERED 1
WORKDIR /app

COPY ./client /app/

RUN pip install django
RUN pip install django-cors-headers


CMD ["python", "manage.py", "runserver","0.0.0.0:8000"]

# To build:
#  docker build . -t client
#
# To run (from this directory)
#  docker run -p 80:8000 client

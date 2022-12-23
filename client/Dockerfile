FROM python:3.9-buster

RUN  mkdir /app
ENV PYTHONUNBUFFERED 1
WORKDIR /app

COPY ./client /app/

RUN pip install django
RUN pip install django-cors-headers


CMD ["python", "manage.py", "runserver","0.0.0.0:8000"]

# To run:
#  docker build . -t client
# docker run -p 8000:8000 client

# From this directory
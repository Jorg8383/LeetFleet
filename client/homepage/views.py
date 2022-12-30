from django.shortcuts import render
from django.http import HttpResponse, request
from django.views.decorators.csrf import csrf_exempt

# Create your views here.


@csrf_exempt
def index(request):
    return render(request, 'index.html')

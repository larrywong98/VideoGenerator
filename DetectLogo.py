import os
import time
from google.cloud import vision_v1
os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = r'service-account-file.json'

client = vision_v1.ImageAnnotatorClient()

logos=[]

with open("response.txt","a") as r:
    for frameIndex in range(6215,6916):
        with open(f"C:\\Users\\larry\\Documents\\VideoGenerator\\dataset\\Allframes\\{frameIndex}.png", 'rb') as image_file:
            content = image_file.read()
        image_file.close()

        image = vision_v1.types.Image(content=content)
        response = client.logo_detection(image=image)
        if response==None:
            continue

        print(frameIndex)
#         print(response.logo_annotations)

        logo=0
        logo_name=["subway","starbucks"]
        for i in range(len(response.logo_annotations)):
            # McDonald\'s
            # American Eagle Outfitters
            # NFL
            # Hard Rock Cafe

            if response.logo_annotations[i].description.lower()==logo_name[0] or response.logo_annotations[i].description.lower()==logo_name[1]:
                r.write(str(frameIndex)+",")
                logo=1
                if logo_name[0] not in logos and response.logo_annotations[i].description.lower()==logo_name[0]:
                    logos.append(f"{frameIndex}")
                    logos.append(logo_name[0])
                if logo_name[1] not in logos and response.logo_annotations[i].description.lower()==logo_name[1]:
                    logos.append(f"{frameIndex}")
                    logos.append(logo_name[1])
                break

        for i in range(len(response.logo_annotations)):
    #         print(response.logo_annotations[i].description)
#             if response.logo_annotations[i].description.lower()==logo_name[0]:

            if response.logo_annotations[i].description.lower()==logo_name[0] or response.logo_annotations[i].description.lower()==logo_name[1]:
                vert=response.logo_annotations[i].bounding_poly.vertices
                for j in range(vert[0].x,vert[1].x+1,1):
                    r.write(str(j)+" "+str(vert[0].y)+",")
                for j in range(vert[1].y,vert[2].y+1,1):
                    r.write(str(vert[1].x)+" "+str(j)+",")
                for j in range(vert[2].x,vert[3].x-1,-1):
                    r.write(str(j)+" "+str(vert[3].y)+",")
                for j in range(vert[3].y,vert[0].y-1,-1):
                    if j==vert[0].y and i==len(response.logo_annotations)-1:
                        r.write(str(vert[3].x)+" "+str(j))
                    else:
                        r.write(str(vert[3].x)+" "+str(j)+",")

        if logo==1:
            r.write("\n")

r.close()
# with open("logoframe.txt","w") as f:
#     for idx,logo in enumerate(logos):
#         if idx==len(logos)-1:
#             f.write(logo)
#         else:
#             f.write(logo+",")
# f.close()

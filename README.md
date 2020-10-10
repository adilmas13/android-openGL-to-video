# Android OpenGL to Video
 
A Simple POC that demonstrates the rendering of a 15 secs video using openGl and android Media APis.

### Operation Pipeline is as follows :
Read Master JSON from res raw.  
=> parse to create ui Data Models  
=> Use the UiModels to draw on TextureView Using openGL.  
=> OnClick of "export" pass the UiModels to VideoRenderer.  
=> VideoRender generates the video using OpenGl, MediaExtractor, MediaCodec, MediaMuxer.  
=> Video is saved to local storage under "opengl-to-video" folder.  

### APIs Used
- OpenGL : to draw the masterJson on the textureView
- MediaExtractor : to the media info like the track Index
- Media Codec : to set the output file media properties
- MediaMetadataRetriever : to get media meta data info like video size, width and height

### Requirements
- External Storage Permission (won't be able to use the app without this)

### Non Errors
- In-app sharing of the video is broken

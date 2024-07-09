async function uploadToServer(formObj) {
    debugger
    console.log("upload to server......")
    console.log(formObj)

    const response = await axios({
        method: 'post',
        url: '/upload',
        data: formObj,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    });

    return response.data
}

async function removeFileToServer(uuid, fileName) {
    debugger
    try {
        const response = await axios.delete(`/remove/${uuid}_${fileName}`)
        return response.data
    } catch (error) {
        alert("에러 : " + error.response ? error.response.data : error.message);
    }

}

